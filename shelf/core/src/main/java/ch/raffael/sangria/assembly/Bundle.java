package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.raffael.sangria.libs.guava.base.Supplier;
import ch.raffael.sangria.libs.guava.base.Suppliers;
import ch.raffael.sangria.libs.guava.collect.ImmutableBiMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.ImmutableSetMultimap;
import ch.raffael.sangria.libs.guava.collect.SetMultimap;

import ch.raffael.sangria.annotations.index.Index;
import ch.raffael.sangria.annotations.index.IndexSyntaxException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Bundle implements ResourceLocator, Messager.Source {

    private static final Logger log = LoggerFactory.getLogger(Bundle.class);
    private final URI uri;
    private final String name;
    private final Supplier<Set<URL>> indexes = Suppliers.memoize(() -> {
        ImmutableSet.Builder<URL> builder = ImmutableSet.builder();
        try {
            Iterator<URL> urls = getResources(Index.RESOURCE_PATH);
            while ( urls.hasNext() ) {
                URL url = urls.next();
                //if ( getParent() != null && !getParent().indexes.contains(url) ) {
                builder.add(url);
                //}
            }
            return builder.build();
        }
        catch ( IOException e ) {
            getAssembly().getMessager().error(Bundle.this, "I/O error scanning index files: " + e.getLocalizedMessage(), e);
            return ImmutableSet.of();
        }
    });
    private final Supplier<Map<String, AssemblyAdvice>> advices = Suppliers.memoize(() -> {
        ImmutableMap.Builder<String, AssemblyAdvice> builder = ImmutableBiMap.builder();
        Set<URL> indexes = Bundle.this.indexes.get();
        for ( URL indexUrl : indexes ) {
            if ( getParent() == null || !getParent().indexes.get().contains(indexUrl) ) {
                log.trace("Reading index from {}", indexUrl);
                try {
                    for ( Index.Entry key : new Index().load(indexUrl) ) {
                        AssemblyAdvice advice = AssemblyAdvice.forKey(Bundle.this, new Index.Entry(key.kind(), key.name()));
                        if ( advice == null ) {
                            getAssembly().getMessager().error(Bundle.this, String.format("Error in index at %s: Cannot locate %s %s", indexUrl, key.kind().label(), key.name()));
                            continue;
                        }
                        builder.put(key.name(), advice);
                    }
                }
                catch ( IndexSyntaxException e ) {
                    getAssembly().getMessager().error(Bundle.this, e.getMessage());
                    return ImmutableMap.of();
                }
                catch ( AssemblyException e ) {
                    getAssembly().getMessager().error(Bundle.this, "Error loading index at " + indexUrl + ": " + e.getMessage(), e);
                    return ImmutableMap.of();
                }
                catch ( Exception e ) {
                    getAssembly().getMessager().error(Bundle.this, "Error reading index from " + indexUrl + ": " + e.getLocalizedMessage(), e);
                    return ImmutableMap.of();
                }
            }
        }
        return builder.build();
    });
    protected final Supplier<Map<String, Feature>> features = Suppliers.memoize(new Supplier<Map<String, Feature>>() {
        @Override
        public Map<String, Feature> get() {
            Map<String, Feature> features = new HashMap<>();
            for ( AssemblyAdvice advice : advices.get().values() ) {
                if ( advice.feature() ) {
                    Feature feature = new Feature(getAssembly(), Bundle.this, advice.name(), advice.provides(), advice.extend());
                    Feature current = features.get(feature.getId());
                    if ( current != null && current.getId().equals(feature.getId()) ) {
                        // duplicate primary feature ID
                        getAssembly().getMessager().error(Bundle.this, "Duplicate feature: " + feature.getId());
                        continue;
                    }
                    if ( !shadowed(feature, feature.getId()) ) {
                        features.put(feature.getId(), feature);
                    }

                    for ( String provision : feature.getProvisions() ) {
                        current = features.get(provision);
                        if ( current != null ) {
                            if ( !current.getId().equals(provision) ) {
                                // duplicate provision
                                getAssembly().getMessager().error(Bundle.this, "Duplicate provision: " + feature.getId());
                            }
                            continue;
                        }
                        if ( !shadowed(feature, provision) ) {
                            features.put(provision, feature);
                        }
                    }
                }
            }
            return features;
        }
        private boolean shadowed(Feature feature, String id) {
            Bundle parent = getParent();
            while ( parent != null ) {
                Feature top = parent.features.get().get(id);
                if ( top != null ) {
                    log.debug(" {} shadows {}", top, feature);
                    return true;
                }
                parent = parent.getParent();
            }
            return false;
        }
    });
    private final Supplier<SetMultimap<String, String>> packageLinks = Suppliers.memoize( () -> {
        ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
        advices.get().values().stream().filter(adv -> adv.kind() == Index.Kind.PACKAGE).forEach( advice -> {
            advice.linkTo().stream().flatMap(linkTo -> {
                try {
                    return QNames.parseQNameList(advice.name(), linkTo).stream();
                }
                catch ( InvalidQNameListException e ) {
                    getAssembly().getMessager().error(Bundle.this, e.getMessage(), e);
                    return Collections.<String>emptySet().stream();
                }
            }).forEach(linkTo -> builder.put(linkTo, advice.name()));
            builder.putAll(advice.name(), advice.outboundLinks().stream().flatMap(extLink -> {
                try {
                    return QNames.parseQNameList(advice.name(), extLink).stream();
                }
                catch ( InvalidQNameListException e ) {
                    getAssembly().getMessager().error(Bundle.this, e.getMessage(), e);
                    return Collections.<String>emptySet().stream();
                }
            }).toArray(String[]::new));
        });
        return builder.build();
    });

    public Bundle(URI uri, String name) {
        this.uri = uri;
        this.name = name;
    }

    public abstract Assembly getAssembly();

    public URI getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public abstract Bundle getParent();

    public abstract ClassLoader getClassLoader();

    public Map<String, Feature> getFeatures() {
        return features.get();
    }

    Set<String> packageLinks(String packageName) {
        return packageLinks.get().get(packageName);
    }

    protected abstract void resolveFeatures();
}
