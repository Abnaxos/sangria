package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import ch.raffael.sangria.libs.guava.collect.Iterators;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class UserBundle extends Bundle {

    private final Bundle parent;

    private final BundleClassLoader classLoader;

    UserBundle(Bundle parent, URI uri, String name, Set<URL> classPath) {
        super(uri, name);
        this.parent = parent;
        this.classLoader = new BundleClassLoader(this, classPath.toArray(new URL[classPath.size()]), parent.getClassLoader(), null);
    }

    @Override
    public String toString() {
        return "bundle:" + getName();
    }

    @Override
    public Assembly getAssembly() {
        return parent.getAssembly();
    }

    @Override
    public Bundle getParent() {
        return parent;
    }
    @Override
    public BundleClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public URL getResource(String name) {
        return classLoader.findResource(name);
    }

    @Override
    public Iterator<URL> getResources(String name) throws IOException {
        return Iterators.forEnumeration(classLoader.findResources(name));
    }

    @Override
    protected void resolveFeatures() {
        for ( Feature feature : features.get().values() ) {
            for ( String packageName : feature.getPackages() ) {
                classLoader.delegate(packageName, feature);
            }
        }
        // FIXME: Not implemented

    }
}
