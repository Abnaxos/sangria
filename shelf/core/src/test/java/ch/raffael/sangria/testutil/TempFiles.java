package ch.raffael.sangria.testutil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.SupportedAnnotationTypes;

import ch.raffael.sangria.dynamic.asm.ClassReader;
import ch.raffael.sangria.dynamic.asm.ClassVisitor;
import ch.raffael.sangria.dynamic.asm.Opcodes;
import ch.raffael.sangria.libs.guava.base.Function;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.Lists;
import ch.raffael.sangria.libs.guava.io.Resources;
import ch.raffael.sangria.libs.guava.reflect.ClassPath;

import ch.raffael.sangria.annotations.index.Index;
import ch.raffael.sangria.annotations.index.IndexAnnotationProcessor;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class TempFiles {

    private static final Set<String> indexAnnotations = ImmutableSet.copyOf(
            Lists.transform(Arrays.asList(IndexAnnotationProcessor.class.getAnnotation(SupportedAnnotationTypes.class).value()),
                            new Function<String, String>() {
                                @Override
                                public String apply(String input) {
                                    return "L" + input.replace('.', '/') + ";";
                                }
                            }));


    private static final Object lock = new Object();
    private static final Map<String, URL> classPaths = new HashMap<>();
    private static Path basePath = null;

    private static Map<String, Integer> counters = new HashMap<>();

    private TempFiles() {
    }

    public static Path createTempDir(String prefix) throws IOException {
        synchronized ( lock ) {
            return Files.createDirectory(tempPath(prefix));
        }
    }

    public static Path createTempFile(String prefix) throws IOException {
        return createTempFile(prefix, "");
    }

    public static Path createTempFile(String prefix, String suffix) throws IOException {
        synchronized ( lock ) {
            return Files.createFile(tempPath(prefix, suffix));
        }
    }

    public static URL prefixClassPath(String prefix) throws IOException {
        synchronized ( lock ) {
            if ( classPaths.get(prefix) != null ) {
                return classPaths.get(prefix);
            }
            Path path = createTempDir(prefix);
            return populateClassPath(path, prefix.replace('.', '/'));
        }
    }

    private static URL populateClassPath(Path path, String prefix) throws IOException {
        Index index = new Index();
        for ( ClassPath.ResourceInfo resource: ClassPath.from(TempFiles.class.getClassLoader()).getResources() ) {
            if ( resource.getResourceName().equals(Index.RESOURCE_PATH) ) {
                index.load(resource.url());
            }
            if ( matchesPrefix(resource.getResourceName(), prefix) ) {
                Path outPath = path.resolve(resource.getResourceName().replace('/', File.separatorChar));
                Files.createDirectories(outPath.getParent());
                byte[] contents = Resources.toByteArray(resource.url());
                //String indexEntry = getIndexEntry(resource, contents);
                //if ( indexEntry != null ) {
                //    index.add(indexEntry);
                //}
                Files.write(outPath, contents);
            }
        }
        index.removeIf((entry) -> !matchesPrefix(entry.name().replace('.', '/'), prefix));
        Path indexPath = path.resolve(Index.RESOURCE_PATH.replace('/', File.separatorChar));
        Files.createDirectories(indexPath.getParent());
        try ( OutputStream out = new BufferedOutputStream(Files.newOutputStream(indexPath)) ) {
            index.write(out);
        }
        URL url=path.toUri( ).toURL();
        classPaths.put(prefix, url);
        return url;
    }

    private static boolean matchesPrefix(String name, String prefix) {
        return name.startsWith(prefix + '/') || name.equals(prefix);
    }

    private static String getIndexEntry(final ClassPath.ResourceInfo resource, byte[] contents) {
        class IndexEntryGetter extends ClassVisitor {
            String indexEntry = null;
            IndexEntryGetter() {
                super(Opcodes.ASM4);
            }
            @Override
            public ch.raffael.sangria.dynamic.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ( indexEntry == null && indexAnnotations.contains(desc) ) {
                    indexEntry = resource.getResourceName().replace('/', '.').substring(0, resource.getResourceName().length() - 6);
                }
                return null;
            }
        }
        ClassReader reader = new ClassReader(contents);
        IndexEntryGetter getter = new IndexEntryGetter();
        reader.accept(getter, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        String indexEntry = getter.indexEntry;
        if ( indexEntry != null ) {
            if ( indexEntry.endsWith(".package-info") ) {
                indexEntry = "package " + indexEntry.substring(0, indexEntry.length() - ".package-info".length());
            }
            else {
                indexEntry = "class " + indexEntry;
            }
        }
        return indexEntry;
    }

    private static Path tempPath(String prefix) throws IOException {
        return tempPath(prefix, "");
    }

    private static Path tempPath(String prefix, String suffix) throws IOException {
        Integer counter = counters.get(prefix);
        if ( counter == null ) {
            counter = 0;
        }
        Path result = basePath().resolve(String.format("%s-%04d%s", prefix, counter, suffix));
        counters.put(prefix, counter + 1);
        return result;
    }

    private static Path basePath() throws IOException {
        if ( basePath == null ) {
            basePath = Files.createTempDirectory("ch.raffael.sangria-");
            Runtime.getRuntime().addShutdownHook(new Thread("Delete temprorary files") {
                @Override
                public void run() {
                    try {
                        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.deleteIfExists(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                Files.deleteIfExists(dir);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                                System.err.print("Error deleting $basePath: $file: ");
                                e.printStackTrace(System.err);
                                return FileVisitResult.TERMINATE;
                            }
                        });
                    }
                    catch ( IOException e ) {
                        System.err.print("Error deleting $basePath: ");
                        e.printStackTrace(System.err);
                    }
                }
            });
        }
        return basePath;
    }

}
