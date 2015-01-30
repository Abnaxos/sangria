package ch.raffael.sangria.bootstrap_old;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import com.google.inject.Inject;

import ch.raffael.sangria.libs.guava.collect.ImmutableSet;

import ch.raffael.sangria.assembly.Assembly;
import ch.raffael.sangria.assembly.Bundle;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class StandardBundleLoader implements BundleLoader {

    private static Set<String> JAR_MIME_TYPES = ImmutableSet.of(
            "application/java-archive",
            "application/x-java-archive",
            "application/x-jar",
            "application/x-java-jar",
            "application/zip");

    public static final String BUNDLE_DIR_EXTENSION = ".bnd";

    private final Assembly assembly;

    @Inject
    public StandardBundleLoader(Assembly assembly) {
        this.assembly = assembly;
    }

    @Override
    public boolean loadBundles(URI uri) throws IOException {
        if ( !"file".equals(uri.getScheme()) ) {
            return false;
        }
        Path path = Paths.get(uri);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if ( dir.endsWith(BUNDLE_DIR_EXTENSION) ) {
                    final ImmutableSet.Builder<URL> classpath = ImmutableSet.builder();
                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if ( JAR_MIME_TYPES.contains(Files.probeContentType(file)) ) {
                                classpath.add(file.toUri().toURL());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    assembly.addBundle(dir.toUri(), dir.getFileName().toString(), classpath.build());
                    return FileVisitResult.SKIP_SUBTREE;
                }
                else {
                    return FileVisitResult.CONTINUE;
                }
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if ( JAR_MIME_TYPES.contains(Files.probeContentType(file)) ) {
                    URI fileUri = file.toUri();
                    Bundle bundle = assembly.addBundle(fileUri, file.getFileName().toString(), ImmutableSet.of(fileUri.toURL()));
                    // found lightweight bundle
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return true;
    }

    public static boolean isJarFile(Path path) throws IOException {
        return Files.isRegularFile(path) && JAR_MIME_TYPES.contains(Files.probeContentType(path));
    }
}
