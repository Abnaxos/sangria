package ch.raffael.sangria.cluster.packaging;

import java.lang.reflect.Array;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.function.Supplier;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class CodeSourceSupplier implements Supplier<CodeSource> {

    private final Object lock = new Object();

    private final URL location;

    public CodeSourceSupplier(URL location) {
        this.location = location;
    }

    public URL getLocation() {
        return location;
    }

    @Override
    public CodeSource get() {
        Object verification = verify();
        if ( verification == null ) {
            // use just any of the two constructors
            return new CodeSource(location, (Certificate[])null);
        }
        else if ( verification instanceof CodeSigner[] ) {
            return new CodeSource(location, (CodeSigner[])verification);
        }
        else if ( verification instanceof Certificate[] ) {
            return new CodeSource(location, (Certificate[])verification);
        }
        else {
            throw illegalVerification();
        }
    }

    /**
     * Verify the resource for the code source and return either the certificates or the code
     * signers or `null`.
     *
     * @return `null` or an array of Certificates or CodeSigners.
     *
     * @throws SecurityException If the resource cannot be verified.
     */
    protected Object verify() {
        return null;
    }

    public CodeSourceSupplier combine(CodeSourceSupplier other) {
        class CombinedCodeSourceSupplier extends CodeSourceSupplier {
            CombinedCodeSourceSupplier(URL location) {
                super(location);
            }
            @Override
            protected Object verify() {
                Object myVerification = CodeSourceSupplier.this.verify();
                if ( myVerification == null ) {
                    return other.verify();
                }
                else if ( myVerification instanceof CodeSigner[] ) {
                    return combine((CodeSigner[])myVerification, other.get().getCodeSigners());
                }
                else if ( myVerification instanceof Certificate[] ) {
                    return combine((Certificate[])myVerification, other.get().getCertificates());
                }
                else {
                    throw illegalVerification();
                }
            }
        }
        return new CombinedCodeSourceSupplier(location);
    }

    @SuppressWarnings("unchecked")
    static <T> T[] combine(T[] primary, T[] secondary) {
        if ( secondary == null || secondary.length == 0 ) {
            return primary;
        }
        LinkedHashSet<T> combined = new LinkedHashSet<>(primary.length + (secondary == null ? 0 : secondary.length));
        combined.addAll(Arrays.asList(primary));
        combined.addAll(Arrays.asList(secondary));
        return combined.toArray((T[])Array.newInstance(primary.getClass().getComponentType(), combined.size()));
    }

    protected IllegalArgumentException illegalVerification() {
        return new IllegalArgumentException("verify() must return null, Certificate[] or CodeSigner[]");
    }

}
