package ch.raffael.sangria.bootstrap;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.ImplementedBy;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

import ch.raffael.sangria.libs.args4j.CmdLineParser;

import ch.raffael.guards.NotNull;
import ch.raffael.sangria.assembly.AssemblyInfo;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AssemblyBuilder {

    private final AssemblyInfo info;
    private final Map<String, BundleBuilder> bundles = new LinkedHashMap<>();
    private final Set<Object> cmdLineTargets = new LinkedHashSet<>();

    public AssemblyBuilder(AssemblyInfo info) {
        this.info = info;
    }

    @NotNull
    public synchronized BundleBuilder newBundle(@NotNull String id) throws BootstrapException {
        BundleBuilder builder = bundles.get(id);
        if ( builder != null ) {
            throw new BootstrapException("Duplicate Bundle id='" + id + "'");
        }
        builder = new BundleBuilder(this, id);
        bundles.put(id, builder);
        return builder;
    }

    @NotNull
    public synchronized BundleBuilder enhanceBundle(@NotNull String id) throws BootstrapException {
        return bundles.computeIfAbsent(id, k -> new BundleBuilder(this, k));
    }

    @SuppressWarnings("ObjectEquality")
    synchronized void remove(BundleBuilder bundle) {
        BundleBuilder current = bundles.get(bundle.getId());
        if ( current != bundle ) {
            bundles.remove(bundle.getId());
        }
    }

    public AssemblyBuilder cmdLineBean(Object bean) {
        return null;
    }

    public AssemblyBuilder cmdLineBean(Class<?> type) {
        return null;
    }

    public AssemblyBuilder cmdLineBean(Class<?> type, Annotation annotation) {
        return null;
    }

    public AssemblyBuilder cmdLineBean(Key key) {
        return null;
    }

    public void cmdLineTargets(Object... targets) {

    }

    public void build(String[] cmdLineArgs, List<Module> modules) {
        CmdLineParser parser = new CmdLineParser(null);
    }

    private final class Module extends AbstractModule {

        @Override
        protected void configure() {
            //ClassToInstanceMap map;
            //map.put
            //Multibinder cmdLineTargets = Multibinder.newSetBinder(binder(), )
            //for ( Object cmdLineTarget : this.cmdLineTargets ) {
            //    if ( cmdLineTarget instanceof Class ) {
            //
            //    }
            //}
        }
    }

    private static class CmdLineTargets {
        private final Map<Class<?>, Provider<?>> targets = new LinkedHashMap<>();

    }

}
