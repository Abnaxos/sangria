package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import ch.raffael.sangria.libs.guava.collect.Iterators;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class RootBundle extends Bundle {

    private final ClassLoader classLoader;
    private final Assembly assembly;

    public RootBundle(ClassLoader classLoader, Assembly assembly) {
        super(URI.create("assembly:/" + encode(assembly.getInfo().getId()) + "/" + assembly.getId().toString()), "assembly:" + assembly.getInfo().getId());
        this.classLoader = classLoader;
        this.assembly = assembly;
    }

    private static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch ( UnsupportedEncodingException e ) {
            throw new IllegalStateException("Unsupported encoding UTF-8");
        }
    }

    @Override
    public Assembly getAssembly() {
        return assembly;
    }

    @Override
    public Bundle getParent() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public URL getResource(String name) {
        return classLoader.getResource(name);
    }

    @Override
    public Iterator<URL> getResources(String name) throws IOException {
        return Iterators.forEnumeration(classLoader.getResources(name));
    }

    @Override
    protected void resolveFeatures() {
        // FIXME: Not implemented
    }
}
