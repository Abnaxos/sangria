package ch.raffael.sangria.cluster.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import ch.raffael.sangria.libs.guava.collect.ImmutableMap;

import ch.raffael.sangria.cluster.packaging.security.SecureCodeSourceSupplier;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractJarResourceResolver implements ResourceResolver {

    private final JarFile jar;
    private final URL location;

    private final Attributes attributes;
    private final ImmutableMap<String, Resource> resources;

    public AbstractJarResourceResolver(JarFile jar, URL location) throws IOException {
        this.jar = jar;
        this.location = location;
        attributes = Attributes.copyOf(jar.getManifest().getMainAttributes());
        ImmutableMap.Builder<String, Resource> resources = ImmutableMap.builder();
        Enumeration<JarEntry> entries = jar.entries();
        while ( entries.hasMoreElements() ) {
            JarEntry entry = entries.nextElement();
            if ( !entry.isDirectory() ) {
                resources.put(entry.getName(), createResource(entry));
            }
        }
        this.resources = resources.build();
    }

    protected JarResource createResource(JarEntry entry) throws IOException {
        return new JarResource(this, entry, location);
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public Collection<Resource> resources() {
        return resources.values();
    }

    @Override
    public Stream<Resource> resourceStream() {
        return resources().stream();
    }

    @Override
    public Resource getResource(String path) {
        return resources.get(path);
    }

    protected static class JarResource implements Resource {

        protected final AbstractJarResourceResolver resolver;
        protected final JarEntry entry;
        private final Attributes attributes;

        private final CodeSourceSupplier codeSource;

        protected JarResource(AbstractJarResourceResolver resolver, JarEntry entry, URL location) throws IOException {
            this.resolver = resolver;
            this.entry = entry;
            attributes = Attributes.copyOf(entry.getAttributes());
            codeSource = new SecureCodeSourceSupplier(location, resolver);
        }

        @Override
        public ResourceResolver getResolver() {
            return resolver;
        }

        @Override
        public String getPath() {
            return entry.getName();
        }

        @Override
        public InputStream openStream() throws IOException {
            return resolver.jar.getInputStream(entry);
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public CodeSource getCodeSource() {
            return codeSource.get();
        }
    }
}
