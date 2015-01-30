package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import ch.raffael.sangria.libs.guava.collect.ImmutableMap;

import ch.raffael.guards.Nullable;

import static ch.raffael.sangria.libs.guava.base.Objects.firstNonNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Assembly {

    private static final Logger log = LoggerFactory.getLogger(Assembly.class);

    private final MessageCollector messageCollector = new MessageCollector();
    private final Messager messager;
    private final ClassLoader classLoader;
    private final UUID id = UUID.randomUUID();
    private final AssemblyInfo assemblyInfo;

    private final RootBundle rootBundle;
    private final Map<URI, Bundle> bundles = new LinkedHashMap<>();
    private final Map<String, Feature> features = new HashMap<>();


    public Assembly(AssemblyInfo assemblyInfo) throws IOException {
        this(assemblyInfo, null, null);
    }

    public Assembly(AssemblyInfo.Builder assemblyInfoBuilder) throws IOException {
        this(assemblyInfoBuilder.build());
    }

    public Assembly(AssemblyInfo assemblyInfo, @Nullable ClassLoader classLoader) throws IOException {
        this(assemblyInfo, null, classLoader);
    }

    public Assembly(AssemblyInfo.Builder assemblyInfoBuilder, @Nullable ClassLoader classLoader) throws IOException {
        this(assemblyInfoBuilder.build(), null, classLoader);
    }

    public Assembly(AssemblyInfo assemblyInfo, @Nullable Messager messager, @Nullable ClassLoader classLoader) throws IOException {
        classLoader = firstNonNull(classLoader, getClass().getClassLoader());
        this.classLoader = classLoader;
        this.assemblyInfo = assemblyInfo;
        if ( messager == null ) {
            this.messager = new MessageBroadcaster(new MessageLogger(log), new MessageCollector());
        }
        else {
            this.messager = new MessageBroadcaster(messager, new MessageCollector());
        }
        rootBundle = new RootBundle(classLoader, this);
        bundles.put(rootBundle.getUri(), rootBundle);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public UUID getId() {
        return id;
    }

    public AssemblyInfo getInfo() {
        return assemblyInfo;
    }

    public Bundle addBundle(URI uri, String name, Set<URL> classPath) throws DuplicateBundleException {
        synchronized ( bundles ) {
            Bundle bundle = bundles.get(uri);
            if ( bundle != null ) {
                throw new DuplicateBundleException(bundle);
            }
            bundle = new UserBundle(rootBundle, uri, name, classPath);
            bundles.put(uri, bundle);
            return bundle;
        }
    }

    @Nullable
    public Bundle getBundle(URI uri) {
        synchronized ( bundles ) {
            return bundles.get(uri);
        }
    }

    public Map<URI, Bundle> getBundles() {
        synchronized ( bundles ) {
            return ImmutableMap.copyOf(bundles);
        }
    }

    public Messager getMessager() {
        return messager;
    }

    public Injector createInjector() {
        determineFeatures();
        resolveFeatures();
        // FIXME: not implemented
        return null;
    }

    private void determineFeatures() {
        // Map all primary feature IDs to features
        for ( Bundle bundle : bundles.values() ) {
            for ( Feature feature : bundle.getFeatures().values() ) {
                if ( Assembly.this.features.containsKey(feature.getId()) ) {
                    // error: duplicate feature
                    messager.error(bundle, "Duplicate feature: " + feature);
                }
                features.put(feature.getId(), feature);
            }
        }
        // Fill in all provided feature IDs, that won't override any primary feature ID
        for ( Feature feature : features.values() ) {
            for ( String provided : feature.getProvisions() ) {
                //noinspection StatementWithEmptyBody
                if ( !features.containsKey(provided) ) {
                    features.put(provided, feature);
                }
                else {
                    // duplicate provided feature
                    // FIXME: Warning? Error? For now, we just use the first one.
                }
            }
        }
    }

    private void resolveFeatures() {
        for ( Bundle bundle : bundles.values() ) {
            bundle.resolveFeatures();
        }
    }

}
