package ch.raffael.sangria.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;

import ch.raffael.sangria.libs.guava.base.Throwables;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;

import ch.raffael.sangria.logging.Logging;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BundleScanner {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logging.logger();

    private final AssemblyBuilder assembly;
    private final Path path;

    private final Set<PathMatcher> includes = new LinkedHashSet<>();
    private final Set<PathMatcher> excludes = new LinkedHashSet<>();

    public BundleScanner(AssemblyBuilder assembly, Path path) {
        this.assembly = assembly;
        this.path = path;
    }

    public void scan() throws IOException, BootstrapException {
        try {
            if ( !loadBundle(path) ) {
                Files.walkFileTree(path, new Visitor());
            }
        }
        catch ( IOException e ) {
            Throwables.propagateIfInstanceOf(e.getCause(), BootstrapException.class);
            throw e;
        }
    }

    public BundleScanner include(Object... matchers) {
        return matchers(includes, matchers);
    }

    public BundleScanner include(Iterable<?> matchers) {
        return matchers(includes, matchers);
    }

    public BundleScanner exclude(Object... matchers) {
        return matchers(excludes, matchers);
    }

    public BundleScanner exclude(Iterable<?> matchers) {
        return matchers(excludes, matchers);
    }

    private BundleScanner matchers(Collection<PathMatcher> target, Object[] specs) {
        return matchers(target, Arrays.asList(specs));
    }

    private BundleScanner matchers(Collection<PathMatcher> target, Iterable<?> specs) {
        for ( Object spec : specs ) {
            target.add(toMatcher(spec));
        }
        return this;
    }

    protected PathMatcher toMatcher(Object spec) {
        if ( spec instanceof PathMatcher ) {
            return (PathMatcher)spec;
        }
        else if ( spec instanceof String ) {
            return path.getFileSystem().getPathMatcher("glob:" + spec);
        }
        return null;
    }


    private class Visitor extends SimpleFileVisitor<Path> {

        private Set<PathMatcher> includes;
        private Set<PathMatcher> excludes;

        private Visitor() {
            if ( BundleScanner.this.includes.isEmpty() ) {
                includes = ImmutableSet.of(path.getFileSystem().getPathMatcher("glob:**/*.bnd"));
            }
            includes = ImmutableSet.copyOf(BundleScanner.this.includes);
            excludes = ImmutableSet.copyOf(BundleScanner.this.excludes);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if ( excludes.stream().anyMatch(m -> m.matches(dir)) ) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            if ( !includes.stream().anyMatch(m -> m.matches(dir)) ) {
                if ( loadBundle(dir) ) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }
            return FileVisitResult.CONTINUE;
        }

    }

    protected boolean loadBundle(Path dir) throws IOException {
        Path bundlePropertiesPath = dir.resolve("bundle.properties");
        if ( Files.isRegularFile(bundlePropertiesPath) ) {
            log.debug("Loading bundle: {}", dir);
            Properties properties = new Properties();
            try ( InputStream input = Files.newInputStream(path) ) {
                properties.load(input);
            }
            catch ( IOException e ) {
                throw new IOException("Cannot load " + bundlePropertiesPath + ": " + e, e);
            }
            if ( !properties.containsKey("id") ) {
                throw new IOException("Cannot load " + bundlePropertiesPath + ": No ID specified");
            }
            BundleBuilder bundle;
            try {
                bundle = assembly.newBundle(properties.getProperty("id"));
            }
            catch ( BootstrapException e ) {
                throw new IOException(e);
            }
            try {
                bundle.source(dir.toUri());
                scanBundle(bundle, dir);
            }
            catch ( Exception e ) {
                throw new IOException(bundle.fail("Error loading bundle " + path, e));
            }
            return true;
        }
        else {
            log.warn("Ignored matching bundle directory {} because it contains no bundle.properties", dir);
        }
        return false;
    }

    protected void scanBundle(final BundleBuilder bundle, Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            final PathMatcher jarMatcher = dir.getFileSystem().getPathMatcher("glob:**/*.jar");
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // TODO: add some checking here
                bundle.classpath(file.toUri().toURL());
                bundle.classpath(file.toUri().toURL(), dir.relativize(file).toString());
                return FileVisitResult.CONTINUE;
            }
        });
    }


}
