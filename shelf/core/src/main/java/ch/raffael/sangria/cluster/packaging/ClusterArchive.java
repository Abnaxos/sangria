package ch.raffael.sangria.cluster.packaging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import ch.raffael.sangria.libs.guava.base.Functions;
import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.collect.Iterables;

import ch.raffael.sangria.cluster.packaging.security.SecureCodeSourceSupplier;
import ch.raffael.sangria.commons.GuavaCollectors;

import static ch.raffael.sangria.commons.Curry.curry;
import static ch.raffael.sangria.commons.Unchecked.throwingCall;
import static ch.raffael.sangria.commons.Unchecked.unchecked;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ClusterArchive implements ClusterFileSystem {

    public static final String ATTR_CLUSTER_ID = "Sangria-Cluster-Id";
    public static final String ATTR_CLUSTER_VERSION = "Sangria-Cluster-Version";
    public static final String ATTR_CLUSTER_DESCRIPTION = "Sangria-Cluster-Description";
    public static final String ATTR_CLUSTER_VENDOR = "Sangria-Cluster-Vendor";
    public static final String ATTR_CLUSTER_LICENSE = "Sangria-Cluster-License";
    public static final String ATTR_CLUSTER_ROOTS = "Sangria-Cluster-Roots";

    // for resource
    private static final Certificate[] NO_CERTIFICATES = new Certificate[0];
    private static final CodeSigner[] NO_CODE_SIGNERS = new CodeSigner[0];

    private final JarFile jar;

    private final String clusterId;
    private final String clusterVersion;
    private final String clusterDescription;
    private final String clusterVendor;
    private final String clusterLicense;

    private final URL location;

    private final JarResourceResolver mainResolver;
    private final ImmutableList<ResourceResolver> roots;

    public ClusterArchive(JarFile jar, URL location) throws IOException {
        this.jar = jar;
        this.location = location;
        mainResolver = new JarResourceResolver(jar);
        clusterId = mainResolver.getAttributes().get(ATTR_CLUSTER_ID);
        if ( clusterId == null ) {
            throw new ClusterFormatException("No Cluster ID found");
        }
        clusterVersion = mainResolver.getAttributes().get(ATTR_CLUSTER_ID);
        if ( clusterVersion == null ) {
            throw new ClusterFormatException("No Cluster version found");
        }
        clusterDescription = mainResolver.getAttributes().get(ATTR_CLUSTER_DESCRIPTION);
        clusterVendor = mainResolver.getAttributes().get(ATTR_CLUSTER_VENDOR);
        clusterLicense = mainResolver.getAttributes().get(ATTR_CLUSTER_LICENSE);
        String rootsAttr = mainResolver.getAttributes().get(ATTR_CLUSTER_ROOTS);
        if ( rootsAttr != null && !rootsAttr.trim().isEmpty() ) {
            roots = throwingCall(IOException.class,
                    () -> Stream.of(rootsAttr.split("\\s+"))
                            .map(root -> root.endsWith("/") ? root : root + "/")
                            .peek((root) -> unchecked(() -> {
                                if ( root.startsWith("/") || root.contains("//") ) {
                                    throw new ClusterFormatException("Invalid root: " + root);
                                }
                            }))
                            .peek(curry(new LinkedList<>(),
                                    (LinkedList<String> seen, String root) -> seen.stream()
                                            .filter(prev -> prev.length() < root.length() ? root.startsWith(prev) : prev.startsWith(root))
                                            .findAny()
                                            .ifPresent(prev -> unchecked(() -> {
                                                throw new ClusterFormatException("Conflicting roots: " + prev + " <-> " + root);
                                            }))))
                            .map((root) -> unchecked(() -> new MappedResolver(root)))
                            .collect(GuavaCollectors.toImmutableList()));
        }
        else {
            roots = ImmutableList.of(mainResolver);
        }
    }

    public ClusterArchive(File jarFile) throws IOException {
        this(new JarFile(jarFile), jarCodeSourceLocation(jarFile));
    }

    public ClusterArchive(File jarFile, boolean verify) throws IOException {
        this(new JarFile(jarFile, verify), jarCodeSourceLocation(jarFile));
    }

    public ClusterArchive(File jarFile, boolean verify, int mode) throws IOException {
        this(new JarFile(jarFile, verify, mode), jarCodeSourceLocation(jarFile));
    }

    public ClusterArchive(Path jarFile) throws IOException {
        this(jarFile.toFile());
    }

    public ClusterArchive(Path jarFile, boolean verify) throws IOException {
        this(jarFile.toFile(), verify);
    }

    public ClusterArchive(Path jarFile, boolean verify, int mode) throws IOException {
        this(jarFile.toFile(), verify, mode);
    }

    public ClusterArchive(String jarFile) throws IOException {
        this(toFile(jarFile));
    }

    public ClusterArchive(String jarFile, boolean verify) throws IOException {
        this(toFile(jarFile), verify);
    }

    public ClusterArchive(String jarFile, boolean verify, int mode) throws IOException {
        this(toFile(jarFile), verify, mode);
    }

    private static URL jarCodeSourceLocation(File file) throws IllegalStateException {
        String url = "jar:" + file.toURI().toString() + "!/";
        try {
            return new URL(url);
        }
        catch ( MalformedURLException e ) {
            throw new IllegalStateException("Invalid code source location: " + url, e);
        }
    }

    private static File toFile(String file) {
        return new File(file);
    }

    @Override
    public String getClusterId() {
        return clusterId;
    }

    @Override
    public String getClusterVersion() {
        return clusterVersion;
    }

    @Override
    public String getClusterDescription() {
        return clusterDescription;
    }

    @Override
    public String getClusterVendor() {
        return clusterVendor;
    }

    @Override
    public String getClusterLicense() {
        return clusterLicense;
    }

    @Override
    public Attributes getAttributes() {
        return mainResolver.getAttributes();
    }

    @Override
    public Iterable<ResourceResolver> resolvers() {
        if ( roots.isEmpty() ) {
            return Collections.singleton(mainResolver);
        }
        else {
            return Iterables.transform(roots, Functions.identity());
        }
    }

    @Override
    public Stream<ResourceResolver> resolverStream() {
        return Stream.of(roots.toArray(new ResourceResolver[roots.size()]));
    }

    @Override
    public void close() throws Exception {
        jar.close();
    }

    private final class MappedResource implements Resource {

        private final MappedResolver resolver;
        private final Attributes attributes;
        private final String prefix;
        private final JarResourceResolver.MappableJarResource backingResource;

        private final CodeSourceSupplier codeSource;

        public MappedResource(MappedResolver resolver, Attributes attributes, String prefix, URL location, JarResourceResolver.MappableJarResource backingResource) throws MalformedURLException {
            this.resolver = resolver;
            this.attributes = attributes;
            this.prefix = prefix;
            this.backingResource = backingResource;
            codeSource = new SecureCodeSourceSupplier(location, resolver).combine(backingResource.mapped.codeSource);
        }

        @Override
        public String getPath() {
            return backingResource.getPath().substring(0, prefix.length() + 1);
        }

        @Override
        public InputStream openStream() throws IOException {
            return backingResource.openStream();
        }

        @Override
        public ResourceResolver getResolver() {
            return resolver;
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

    private class JarResourceResolver extends AbstractJarResourceResolver {

        private JarResourceResolver(JarFile jar) throws IOException {
            super(jar, location);
        }

        @Override
        protected JarResource createResource(JarEntry entry) throws IOException {
            return new MappableJarResource(this, entry);
        }

        @Override
        public String getId() {
            return "/";
        }

        @Override
        public ClusterFileSystem getFileSystem() {
            return ClusterArchive.this;
        }

        private class MappableJarResource extends AbstractJarResourceResolver.JarResource {

            private MappedResource mapped;

            private MappableJarResource(JarResourceResolver resolver, JarEntry entry) throws IOException {
                super(resolver, entry, location);
            }

            private void map(MappedResource to) {
                if ( mapped != null ) {
                    throw new IllegalStateException();
                }
                mapped = to;
            }

        }

    }

    private class MappedResolver implements ResourceResolver {

        private final String prefix;
        private final Attributes attributes;

        private final URL location;

        private MappedResolver(String prefix) throws IOException {
            this.prefix = prefix;
            location = new URL(ClusterArchive.this.location, prefix);
            Resource manifestResource = mainResolver.getResource(prefix + JarFile.MANIFEST_NAME);
            Manifest manifest;
            if ( manifestResource != null ) {
                try ( InputStream in = manifestResource.openStream() ) {
                    manifest = new Manifest(in);
                }
            }
            else {
                manifest = new Manifest();
            }
            attributes = Attributes.copyOf(manifest.getMainAttributes());
            //ImmutableMap.Builder<String, Resource> resources = ImmutableMap.builder();
            for ( Resource resource : mainResolver.resources() ) {
                if ( resource.getPath().startsWith(prefix) ) {
                    String path = resource.getPath().substring(0, prefix.length() + 1);
                    MappedResource mapped = new MappedResource(this,
                            Attributes.copyOf(manifest.getAttributes(path)),
                            prefix, location, (JarResourceResolver.MappableJarResource)resource);
                    ((JarResourceResolver.MappableJarResource)resource).map(mapped);
                }
            }
        }

        @Override
        public String getId() {
            return prefix;
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public ClusterFileSystem getFileSystem() {
            return ClusterArchive.this;
        }

        @Override
        public Iterable<Resource> resources() {
            return Iterables.filter(mainResolver.resources(), this::isMappedToThis);
        }

        @Override
        public Stream<Resource> resourceStream() {
            return mainResolver.resourceStream().filter(this::isMappedToThis);
        }

        private boolean isMappedToThis(Resource res) {
            return MappedResolver.this.equals(((JarResourceResolver.MappableJarResource)res).mapped.getResolver());
        }

        @Override
        public Resource getResource(String path) {
            return mainResolver.getResource(prefix+path);
        }

    }

}
