package ch.raffael.sangria.environment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.util.Providers;

import ch.raffael.sangria.libs.guava.base.Strings;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;

import ch.raffael.sangria.annotations.bindings.AppInfo;
import ch.raffael.sangria.annotations.bindings.StandardLocation;
import ch.raffael.sangria.modules.shutdown.ShutdownAdapter;
import ch.raffael.sangria.modules.shutdown.ShutdownCoordinator;
import ch.raffael.sangria.dynamic.Annotations;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class StandardLocationsModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(StandardLocationsModule.class);
    private static final Map<String, Class<? extends Provider<? extends URI>>> STANDARD_PROVIDERS =
            ImmutableMap.<String, Class<? extends Provider<? extends URI>>>of(
                    StandardLocation.DATA, DataUriProvider.class,
                    StandardLocation.CONF, ConfUriProvider.class,
                    StandardLocation.CACHE, CacheUriProvider.class);

    private final Map<String, URI> mappings = new LinkedHashMap<>();

    public StandardLocationsModule() {
        mappings.put(StandardLocation.APP_HOME, Paths.get(System.getProperty("user.dir")).toUri());
        for ( String name : STANDARD_PROVIDERS.keySet() ) {
            mappings.put(name, null);
        }
    }


    public StandardLocationsModule set(String name, URI uri) {
        if ( StandardLocation.USER_HOME.equals(name) ) {
            throw new IllegalArgumentException("Cannot change user's home directory");
        }
        mappings.put(name, uri);
        return this;
    }

    public StandardLocationsModule set(String name, String uri) {
        return set(name, URI.create(uri));
    }

    @Override
    protected void configure() {
        bindStandardLocation(StandardLocation.USER_HOME, userHome().toUri());
        for ( Map.Entry<String, URI> mapping : mappings.entrySet() ) {
            bindStandardLocation(mapping.getKey(), mapping.getValue(), STANDARD_PROVIDERS.get(mapping.getKey()));
        }
    }

    private void bindStandardLocation(String locationName, URI configured) {
        bindStandardLocation(locationName, configured, null);
    }

    private void bindStandardLocation(String locationName, URI configured, Class<? extends Provider<? extends URI>> provider) {
        StandardLocation locationAnnotation = standardLocation(locationName);
        Provider<URI> uriProvider;
        if ( configured != null ) {
            URI uri = mappings.get(StandardLocation.APP_HOME).resolve(configured);
            bind(URI.class).annotatedWith(locationAnnotation).toInstance(uri);
            uriProvider = Providers.of(uri);
        }
        else {
            assert provider != null;
            bind(URI.class).annotatedWith(locationAnnotation).toProvider(provider);
            uriProvider = binder().getProvider(Key.get(URI.class, locationAnnotation));
        }
        bindLocationConverters(locationAnnotation);
        bind(URL.class).annotatedWith(locationAnnotation).toProvider(new UrlConverter(uriProvider));
        bind(File.class).annotatedWith(locationAnnotation).toProvider(new FileConverter(uriProvider));
        bind(Path.class).annotatedWith(locationAnnotation).toProvider(new PathConverter(uriProvider));
    }

    private void bindLocationConverters(StandardLocation locationAnnotation) {
    }

    private StandardLocation standardLocation(String name) {
        return Annotations.forValue(StandardLocation.class, name);
    }

    private static Path winAppData() {
        return firstNonNull(pathFromEnv("APPDATA"), userHome());
    }

    private static Path userHome() {
        return Paths.get(System.getProperty("user.home")).toAbsolutePath();
    }

    private static Path winLocalAppData() {
        return firstNonNull(pathFromEnv("LOCALAPPDATA"), pathFromEnv("APPDATA"), userHome());
    }

    @SuppressWarnings("CallToSystemGetenv")
    private static Path pathFromEnv(String envVariableName) {
        String path = System.getenv(envVariableName);
        if ( !Strings.isNullOrEmpty(path) ) {
            return Paths.get(path).toAbsolutePath();
        }
        else {
            return null;
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... objects) {
        for ( T o : objects ) {
            if ( o != null ) {
                return o;
            }
        }
        throw new NullPointerException("All references are null");
    }

    private static class DataUriProvider implements Provider<URI> {
        private final String appId;
        @Inject
        private DataUriProvider(@AppInfo(AppInfo.Element.ID) String appId) {
            this.appId = appId;
        }
        @Override
        public URI get() {
            switch ( OS.current() ) {
                case WINDOWS:
                    return winAppData().resolve(appId).resolve("data").toUri();
                case MAC:
                    // FIXME: mac implementation
                default:
                    return firstNonNull(pathFromEnv("XDG_DATA_HOME"), userHome().resolve(".local").resolve("share")).resolve(appId).toUri();
            }
        }
    }

    private static class ConfUriProvider implements Provider<URI> {
        private final String appId;
        @Inject
        private ConfUriProvider(@AppInfo(AppInfo.Element.ID) String appId) {
            this.appId = appId;
        }
        @Override
        public URI get() {
            switch ( OS.current() ) {
                case WINDOWS:
                    return winAppData().resolve(appId).resolve("conf").toAbsolutePath().toUri();
                case MAC:
                    // FIXME: mac implementation
                default:
                    return firstNonNull(pathFromEnv("XDG_CONFIG_HOME"), userHome().resolve(".config")).resolve(appId).toUri();
            }
        }
    }

    private static class CacheUriProvider implements Provider<URI> {
        private final String appId;
        @Inject
        private CacheUriProvider(@AppInfo(AppInfo.Element.ID) String appId) {
            this.appId = appId;
        }
        @Override
        public URI get() {
            switch ( OS.current() ) {
                case WINDOWS:
                    return winLocalAppData().resolve(appId).resolve("cache").toUri();
                case MAC:
                    // FIXME: mac implementation
                default:
                    return firstNonNull(pathFromEnv("XDG_CACHE_HOME"), userHome().resolve(".cache")).resolve(appId).toUri();
            }
        }
    }

    private static class TempUriProvider implements Provider<URI> {

        private final String appId;
        private final ShutdownCoordinator shutdownCoordinator;

        @Inject
        private TempUriProvider(String appId, ShutdownCoordinator shutdownCoordinator) {
            this.appId = appId;
            this.shutdownCoordinator = shutdownCoordinator;
        }

        @Override
        public URI get() {
            try {
                final Path temp = Files.createTempDirectory(appId + "-");
                shutdownCoordinator.addShutdownListener(new ShutdownAdapter() {
                    @Override
                    public void postShutdown() {
                        try {
                            if ( Files.exists(temp) ) {
                                Files.walkFileTree(temp, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        Files.delete(file);
                                        return FileVisitResult.CONTINUE;
                                    }
                                    @Override
                                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                        if ( exc == null ) {
                                            Files.delete(dir);
                                            return FileVisitResult.CONTINUE;
                                        }
                                        else {
                                            throw exc;
                                        }
                                    }
                                });
                            }
                        }
                        catch ( IOException e ) {
                            log.error("Error deleting temporary data in directory {}", temp, e);
                        }
                    }
                });
                return temp.toUri();
            }
            catch ( IOException e ) {
// FIXME: Handle exception
                e.printStackTrace();
            }
            // FIXME: Not implemented
            return null;
        }
    }

    private static abstract class ConvertingProvider<T> implements Provider<T> {
        private final String targetType;
        private final Provider<URI> uriProvider;
        private ConvertingProvider(String targetType, Provider<URI> uriProvider) {
            this.uriProvider = uriProvider;
            this.targetType = targetType;
        }
        @Override
        public T get() {
            URI uri = uriProvider.get();
            try {
                return convert(uri);
            }
            catch ( Exception e ) {
                throw new ProvisionException("Cannot convert URI " + uri + " to " + targetType, e);
            }
        }
        protected abstract T convert(URI uri) throws Exception;
    }

    private static class UrlConverter extends ConvertingProvider<URL> {
        private UrlConverter(Provider<URI> uriProvider) {
            super("URL", uriProvider);
        }
        @Override
        protected URL convert(URI uri) throws Exception {
            return uri.toURL();
        }
    }

    private static class FileConverter extends ConvertingProvider<File> {
        private FileConverter(Provider<URI> uriProvider) {
            super("File", uriProvider);
        }
        @Override
        protected File convert(URI uri) throws Exception {
            return new File(uri);
        }
    }

    private static class PathConverter extends ConvertingProvider<Path> {
        private PathConverter(Provider<URI> uriProvider) {
            super("Path", uriProvider);
        }
        @Override
        protected Path convert(URI uri) throws Exception {
            return Paths.get(uri);
        }
    }

}
