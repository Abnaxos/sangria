package ch.raffael.sangria.assembly;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ch.raffael.sangria.libs.guava.base.Supplier;
import ch.raffael.sangria.libs.guava.base.Suppliers;
import ch.raffael.sangria.libs.guava.collect.HashMultimap;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.SetMultimap;

import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Feature {

    private final Assembly assembly;
    private final Bundle provider;

    private final String id;
    private final Supplier<Set<String>> packages = Suppliers.memoize(new Supplier<Set<String>>() {
        @Override
        public Set<String> get() {
            LinkedHashSet<String> packages = new LinkedHashSet<>();
            collect(id, packages);
            return ImmutableSet.copyOf(packages);
        }
        private void collect(String packageName, Set<String> destination) {
            if ( !destination.add(packageName) ) {
                return;
            }
            for ( String linked : provider.packageLinks(packageName) ) {
                collect(linked, destination);
            }
        }
    });
    private final Set<String> provisions;
    private final Supplier<Map<String, Feature>> expandedFeatures;
    private final SetMultimap<String, String> packageLinks = HashMultimap.create();

    public Feature(Assembly assembly, @Nullable Bundle provider, String id, Set<String> provisions, final Set<String> expandedFeatures) {
        assert provider == null || provider.getAssembly().equals(assembly);
        this.assembly = assembly;
        this.id = id;
        this.provider = provider;
        this.provisions = ImmutableSet.copyOf(provisions);
        this.expandedFeatures = Suppliers.memoize(() -> {
            // FIXME: Not implemented
            return ImmutableMap.of();
        });
    }

    @Override
    public String toString() {
        return "feature:" + id + "[" + provider + "]";
    }

    public Assembly getAssembly() {
        return assembly;
    }

    public Bundle getProvider() {
        return provider;
    }

    public String getId() {
        return id;
    }

    public Set<String> getProvisions() {
        return provisions;
    }

    public Set<String> getPackages() {
        return packages.get();
    }

}
