package ch.raffael.sangria;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import ch.raffael.sangria.libs.guava.collect.Iterables;

import ch.raffael.sangria.assembly.Bundle;

import static ch.raffael.sangria.libs.guava.base.Objects.firstNonNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Loader {

    private final ClassLoader classLoader;
    private final Set<URL> topLevelModuleIndexes = new HashSet<>();
    private final List<Object> configComponents = new LinkedList<>();

    public Loader() {
        this(null);
    }

    public Loader(ClassLoader classLoader) {
        this.classLoader = firstNonNull(classLoader, firstNonNull(Thread.currentThread().getContextClassLoader(), getClass().getClassLoader()));
    }

    public BundleBuilder addBundle(URI uri) {
        BundleBuilderImpl builder = new BundleBuilderImpl(uri);
        configComponents.add(builder);
        return builder;
    }

    public Loader addModule(Module module) {
        configComponents.add(module);
        return this;
    }

    public Loader addModules(Module... modules) {
        configComponents.addAll(Arrays.asList(modules));
        return this;
    }

    public Loader addModules(Iterable<Module> modules) {
        Iterables.addAll(configComponents, modules);
        return this;
    }

    public Loader addModuleClass(Class<? extends Module> module) {
        configComponents.add(module);
        return this;
    }

    public Loader addModuleClasses(Class<? extends Module>... modules) {
        configComponents.addAll(Arrays.asList(modules));
        return this;
    }

    public Loader addModuleClasses(Iterable<Class<? extends Module>> modules) {
        Iterables.addAll(configComponents, modules);
        return this;
    }

    public Injector createInjector(Stage stage) {
        //Assembly.Builder assemblyBuilder = Assembly.newBuilder(classLoader, )
        //Assembly assembly = new Assembly();
        //List<Object> configComponents = new LinkedList<>();
        //for ( Object comp : this.configComponents ) {
        //    if ( comp instanceof BundleBuilder ) {
        //        URI uri = ((BundleBuilder)comp).getUri();
        //        Set<URL> cp = ((BundleBuilder)comp).classPath();
        //        Bundle bundle = new Bundle(assembly, uri, new BundleClassLoader(
        //                assembly, uri, cp.toArray(new URL[cp.size()]), classLoader, null));
        //        configComponents.add(bundle);
        //    }
        //    else {
        //        configComponents.add(comp);
        //    }
        //}
        //ConfigurationModule configurationModule = new ConfigurationModule(configComponents);
        //return configurationModule.createAppInjector(stage, Guice.createInjector(stage, configurationModule));
        return null;
    }

    private final class BundleBuilderImpl implements BundleBuilder {

        private final URI uri;
        private final LinkedHashSet<URL> classPath = new LinkedHashSet<>();

        public BundleBuilderImpl(URI uri) {
            this.uri = uri;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public BundleBuilder addToClassPath(URL url) {
            classPath.add(url);
            return this;
        }

        @Override
        public BundleBuilder addToClassPath(URL... urls) {
            classPath.addAll(Arrays.asList(urls));
            return this;
        }

        @Override
        public BundleBuilder addToClassPath(Iterable<URL> urls) {
            Iterables.addAll(classPath, urls);
            return this;
        }

        @Override
        public Set<URL> classPath() {
            return classPath;
        }

        public Loader loader() {
            return Loader.this;
        }

        private Bundle build() {
            return null;
        }

    }

}
