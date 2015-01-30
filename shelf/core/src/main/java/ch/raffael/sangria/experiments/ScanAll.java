package ch.raffael.sangria.experiments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ch.raffael.sangria.libs.guava.base.Stopwatch;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.Iterators;

import ch.raffael.sangria.assembly.AssemblyAdvice;
import ch.raffael.sangria.assembly.ResourceLocator;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ScanAll {

    private final static ForkJoinPool fjPool = new ForkJoinPool();

    public static void main(String[] args) {
        System.err.println("Scanning...");
        Stopwatch sw = Stopwatch.createStarted();
        Stopwatch subSW = Stopwatch.createStarted();
        try {
            LinkedList<Path> jars = new LinkedList<>();
            for ( String path : args ) {
                jars.addAll(scanJars(Paths.get(path)));
            }
            subSW.stop();
            System.err.println("Found " + jars.size() + " JAR files in " + subSW);
            subSW.reset().start();
            int count = 0;
            for ( Path jar : jars ) {
                count += scanClasses(jar);
            }
            subSW.stop();
            sw.stop();
            System.err.println("Scanned " + count + " elements in " + subSW);
            System.err.println("Total time: " + sw);
        }
        catch ( Exception e ) {
            sw.stop();
            e.printStackTrace();
        }
    }
    //private static void scanParallel(final Path path) throws IOException {
    //    ForkJoinPool jfPool = new ForkJoinPool();
    //
    //}

    //private static Future<Integer> scanParallel(final Path path) {
    //    try {
    //        final LinkedList<Future<Integer>> futures = new LinkedList<>();
    //        Files.walkFileTree(path, ImmutableSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
    //            @Override
    //            public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
    //                if ( file.toString().endsWith(".jar") ) {
    //                    fjPool.submit(new Callable<Integer>() {
    //                        @Override
    //                        public Integer call() throws Exception {
    //                            return scanJarFile(file);
    //                        }
    //                    });
    //                }
    //                return FileVisitResult.CONTINUE;
    //            }
    //
    //        });
    //        int count = 0;
    //        while ( !futures.isEmpty() ) {
    //            count += futures.removeFirst().get();
    //        }
    //    }
    //    catch ( Exception e ) {
    //        e.printStackTrace();
    //        return Futures.immediateFailedFuture(e);
    //    }
    //    return Futures.immediateFuture(count[0]);
    //}

    private static List<Path> scanJars(final Path path) throws IOException {
        final LinkedList<Path> jars = new LinkedList<>();
        Files.walkFileTree(path, ImmutableSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
                if ( file.toString().endsWith(".jar") ) {
                    jars.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

        });
        return jars;
    }

    private static int scanClasses(Path file) throws IOException {
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

}
