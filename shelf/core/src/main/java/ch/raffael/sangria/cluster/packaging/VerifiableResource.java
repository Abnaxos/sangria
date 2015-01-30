package ch.raffael.sangria.cluster.packaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Optional;
import java.util.function.Supplier;

import ch.raffael.sangria.commons.Suppliers;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class VerifiableResource {

    private final URL location;
    private final ResourceReader reader;
    private SoftReference<byte[]> cachedContent = null;
    private Supplier<Optional<CodeSource>> codeSource = Suppliers.transientLazy(this::loadCodeSource);

    public VerifiableResource(URL location, ResourceReader reader) {
        this.location = location;
        this.reader = reader;
    }

    public InputStream openInputStream() throws IOException {
        return openInputStream(false);
    }

    public InputStream openInputStream(boolean loadToCache) throws IOException {
        byte[] cache = cachedContent.get();
        if ( cache == null && loadToCache ) {
            try ( InputStream input = reader.openInputStream() ) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int count;
                while ( (count = input.read(buf)) >= 0 ) {
                    out.write(buf, 0, count);
                }
                cache = out.toByteArray();
                cachedContent = new SoftReference<>(cache);
            }
        }
        if ( cache != null ) {
            return new ByteArrayInputStream(cache);
        }
        else {
            return reader.openInputStream();
        }
    }

    private CodeSource getCodeSource() {
        return codeSource.get().orElseThrow(() -> new SecurityException("Resource verification failed"));
    }

    protected Optional<CodeSource> loadCodeSource() {
        // FIXME: see, check signatures etc.
        return Optional.of(new CodeSource(this.location, (CodeSigner[])null));
    }

    @FunctionalInterface
    public static interface ResourceReader {
        InputStream openInputStream() throws IOException;
    }

}
