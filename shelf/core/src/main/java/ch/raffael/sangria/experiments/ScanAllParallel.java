package ch.raffael.sangria.experiments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ch.raffael.sangria.libs.guava.base.Throwables;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.Iterators;

import ch.raffael.sangria.assembly.AssemblyAdvice;
import ch.raffael.sangria.assembly.ResourceLocator;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ScanAllParallel {

    private final static ForkJoinPool fjPool = new ForkJoinPool();

    public static void main(final String[] args) throws Exception {
        ////System.err.println("Scanning...");
        ////Stopwatch sw = Stopwatch.createStarted();
        ////int count = fjPool.submit(() -> {
        ////    Adder adder = new Adder();
        ////    for ( final String arg : args ) {
        ////        adder.add(findJarsTask(Paths.get(arg)).fork());
        ////    }
        ////    return adder.sum();
        ////}).get();
        //sw.stop();
        //System.err.println("Scanned " + count + " elements in " + sw);
    }

    private static RecursiveTask<Integer> findJarsTask(final Path path) {
        return new RecursiveTask<Integer>() {
            @Override
            protected Integer compute() {
                final Adder adder = new Adder();
                try {
                    Files.walkFileTree(path, ImmutableSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
                            if ( file.toString().endsWith(".jar") ) {
                                adder.add(scanClassFilesTask(file).fork());
                            }
                            return FileVisitResult.CONTINUE;
                        }

                    });
                    return adder.sum();
                }
                catch ( IOException e ) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }

    private static RecursiveTask<Integer> scanClassFilesTask(final Path file) {
        return new RecursiveTask<Integer>() {
            @Override
            protected Integer compute() {
                try {
                    JarFile jarFile = new JarFile(file.toFile());
                    Enumeration<JarEntry> entryEnum = jarFile.entries();
                    final int[] count = { 0 };
                    while ( entryEnum.hasMoreElements() ) {
                        JarEntry entry = entryEnum.nextElement();
                        if ( !entry.isDirectory() && entry.getName().endsWith(".class") ) {
                            final String urlBase = "jar:file:" + file + "!/";
                            ResourceLocator locator = new ResourceLocator() {
                                @Override
                                public URL getResource(String name) {
                                    try {
                                        count[0]++;
                                        return new URL(urlBase + name);
                                    }
                                    catch ( MalformedURLException e ) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                }

                                @Override
                                public Iterator<URL> getResources(String name) throws IOException {
                                    return Iterators.singletonIterator(getResource(name));
                                }
                            };
                            if ( entry.getName().endsWith("/package-info.class") ) {
                                AssemblyAdvice.forPackage(locator, entry.getName().substring(0, entry.getName().length() - "/package-info.class".length()).replace('/', '.'));
                            }
                            else {
                                AssemblyAdvice.forClass(locator, entry.getName().substring(0, entry.getName().length() - ".class".length()).replace('/', '.'));
                            }
                        }
                    }
                    return count[0];
                }
                catch ( IOException e ) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }

    private static class Adder extends LinkedList<Future<Integer>> {
        public int sum() {
            int sum = 0;
            for ( Future<Integer> futureInt : this ) {
                try {
                    sum += futureInt.get();
                }
                catch ( Exception e ) {
                    throw Throwables.propagate(e);
                }
            }
            return sum;
        }
    }

}
