package ch.raffael.sangria.bootstrap_old;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Module;

import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.collect.Maps;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.sangria.Loader;

import static ch.raffael.sangria.libs.guava.base.Objects.firstNonNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Bootstrapper {

    private final List<Module> userModules;
    private Iterable<URI> loggingConfigurationSources = ImmutableList.of();

    private Bootstrapper(@NotNull Iterable<Module> userModules) {
        this.userModules = ImmutableList.copyOf(userModules);
    }



    @NotNull
    public static Bootstrapper create() {
        return create(ImmutableList.<Module>of());
    }

    @NotNull
    public static Bootstrapper create(@Nullable Module... modules) {
        return create(asList(modules));
    }

    @NotNull
    public static Bootstrapper create(@Nullable Iterable<Module> modules) {
        return new Bootstrapper(firstNonNull(modules, ImmutableList.<Module>of()));
    }

    @NotNull
    public Bootstrapper configureFromProperties(Path path) throws IOException {
        return configureFromProperties(path, false);
    }

    @NotNull
    public Bootstrapper configureFromProperties(Path path, boolean optional) throws IOException {
        try ( InputStream input = new BufferedInputStream(Files.newInputStream(path)) ) {
            return configureFromProperties(input);
        }
        catch ( FileNotFoundException e ) {
            if ( !optional ) {
                throw e;
            }
        }
        return this;
    }

    @NotNull
    public Bootstrapper configureFromProperties(URL url) throws IOException {
        try ( InputStream input = new BufferedInputStream(url.openStream()) ) {
            return configureFromProperties(input);
        }
    }

    @NotNull
    public Bootstrapper configureFromProperties(InputStream stream) throws IOException {
        return configureFromProperties(stream, "(unknown source)");
    }

    @NotNull
    public Bootstrapper configureFromProperties(InputStream stream,  @Nullable final String source) throws IOException {
        final Properties properties = new Properties();
        properties.load(stream);
        configureFrom(properties, source);
        return this;
    }

    public Bootstrapper configureFromSystemProperties() {
        return configureFrom(System.getProperties(), "(system properties)");
    }

    public Bootstrapper configureFromSystemEnvironment() {
        return configureFrom(System.getenv(), "(environment)");
    }

    public Bootstrapper configureFrom(final Properties properties) {
        return configureFrom(properties, null);
    }

    public Bootstrapper configureFrom(final Properties properties, @Nullable String source) {
        return configureFrom(Maps.fromProperties(properties), source);
    }

    public Bootstrapper configureFrom(final Map<String, String> configuration) {
        return configureFrom(configuration, null);
    }

    public Bootstrapper configureFrom(final Map<String, String> configuration, @Nullable String source) {
        return configureUsing(new ConfigurationLoader() {
            @Override
            public void loadConfiguration(Receiver receiver) throws IOException {
                for ( Map.Entry<String, String> entry : configuration.entrySet() ) {
                    receiver.set(entry.getKey(), entry.getValue());
                }
            }
        }, source == null ? "unknown location" : source);
    }

    public Bootstrapper configureUsing(ConfigurationLoader loader) {
        return configureUsing(loader, null);
    }

    public Bootstrapper configureUsing(ConfigurationLoader loader, @Nullable String source) {
        if ( source == null ) {
            source = loader.toString();
        }
        // FIXME: implement this
        return this;
    }

    @NotNull
    public Loader loader() {
        // FIXME: implement this
        return new Loader();
    }

    @NotNull
    public Loader loader(@Nullable ClassLoader loader) {
        // FIXME: implement this
        return new Loader();
    }

    private static <T> List<T> asList(T[] array) {
        return array != null ? ImmutableList.copyOf(array) : ImmutableList.<T>of();
    }

}
