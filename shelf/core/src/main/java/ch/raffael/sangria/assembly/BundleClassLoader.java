package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import ch.raffael.sangria.libs.guava.collect.MapMaker;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ObjectEquality")
public class BundleClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    public static final Pattern PACKAGE_RE = Pattern.compile(
            "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*");

    private final Bundle bundle;
    private final ConcurrentMap<String, Delegation> delegations = new MapMaker().concurrencyLevel(1).makeMap();

    public BundleClassLoader(Bundle bundle, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.bundle = bundle;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + bundle + "}";
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean delegate(String packageName, Feature feature) {
        Delegation current = delegations.get(packageName);
        if ( current == null ) {
            current = new Delegation(packageName, feature);
            Delegation prev = delegations.putIfAbsent(packageName, current);
            if ( prev != null ) {
                current = prev;
            }
        }
        ClassLoader loader = feature.getProvider().getClassLoader();
        if ( loader != current.loader ) {
            getBundle().getAssembly().getMessager().error(getBundle(), "Delegation of package " + packageName + " to " + feature + " conflicts with " + current.features);
            return false;
        }
        else {
            current.features.add(feature);
            return true;
        }
    }

    public URL getClassFile(String className) {
        return findResource(className.replace('.', '/') + ".class");
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        ClassLoader delegate = getDelegateFor(packageName(name, '.'));
        if ( delegate != null ) {
            try {
                clazz = delegate.loadClass(name);
            }
            catch ( ClassNotFoundException e ) {
                // ignore
            }
        }
        if ( clazz != null ) {
            return clazz;
        }
        else {
            return super.loadClass(name);
        }
    }

    @Override
    public URL findResource(String name) {
        URL resource = null;
        ClassLoader delegate = getDelegateFor(packageName(name, '/'));
        if ( delegate != null ) {
            resource = delegate.getResource(name);
        }
        if ( resource != null ) {
            return resource;
        }
        else {
            return super.findResource(name);
        }
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        ClassLoader delegate = getDelegateFor(packageName(name, '/'));
        if ( delegate == null ) {
            delegate = getParent();
        }
        return new UniqueEnumeration(new ResourceEnumeration(
                delegate.getResources(name),
                super.findResources(name)));
    }

    private ClassLoader getDelegateFor(String packageName) {
        Delegation delegation = delegations.get(packageName);
        if ( delegation == null || delegation.loader == this ) {
            return null;
        }
        else {
            return delegation.loader;
        }
    }

    private static String packageName(String name, char separator) {
        int pos = name.lastIndexOf(separator);
        if ( pos < 0 ) {
            return "";
        }
        else {
            String packageName = name.substring(0, pos);
            if ( separator == '.' ) {
                return packageName;
            }
            else {
                return packageName.replace(separator, '.');
            }
        }
    }



    private static final class UniqueEnumeration implements Enumeration<URL> {

        private final Enumeration<URL> delegate;
        private boolean onNext = false;
        private URL element;
        private Set<URL> seen = new HashSet<>();

        private UniqueEnumeration(Enumeration<URL> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasMoreElements() {
            if ( onNext ) {
                return seen != null;
            }
            onNext = true;
            do {
                if ( delegate.hasMoreElements() ) {
                    element = delegate.nextElement();
                }
                else {
                    seen = null;
                    return false;
                }
            } while ( !seen.add(element) );
            return true;
        }

        @Override
        public URL nextElement() {
            if ( !hasMoreElements() ) {
                throw new NoSuchElementException();
            }
            onNext = false;
            return element;
        }
    }

    private static final class ResourceEnumeration implements Enumeration<URL> {
        private final Enumeration<URL>[] enums;
        private int index = 0;
        private ResourceEnumeration(Enumeration<URL>... enums) {
            this.enums = enums;
        }
        @Override
        public boolean hasMoreElements() {
            while ( index < enums.length ) {
                if ( enums[index] != null && enums[index].hasMoreElements() ) {
                    return true;
                }
                index++;
            }
            return false;
        }

        @Override
        public URL nextElement() {
            if ( !hasMoreElements() ) {
                throw new NoSuchElementException();
            }
            return enums[index].nextElement();
        }
    }

    private static class Delegation {
        private final String packageName;
        private final ClassLoader loader;
        private final Set<Feature> features = new LinkedHashSet<>();
        private Delegation(String packageName, Feature feature) {
            this.packageName = packageName;
            features.add(feature);
            loader = feature.getProvider().getClassLoader();
        }
    }

}
