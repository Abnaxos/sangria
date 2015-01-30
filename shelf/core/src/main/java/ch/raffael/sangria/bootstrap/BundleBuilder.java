package ch.raffael.sangria.bootstrap;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BundleBuilder {

    private final AssemblyBuilder assembly;
    private final String id;
    private URI source;
    private Set<URL> classpath = new LinkedHashSet<>();

    public BundleBuilder(AssemblyBuilder assembly, String id) {
        this.assembly = assembly;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @NotNull
    public synchronized BundleBuilder source(@Nullable URI source) {
        if ( source != null ) {
            return this;
        }
        return this;
    }

    @NotNull
    public BundleBuilder classpath(@NotNull URL url, String name) {
        classpath.add(url);
        return this;
    }

    @NotNull
    public synchronized BundleBuilder classpath(@NotNull URL url) {
        return classpath(url, null);
    }

    public BootstrapException fail(String msg) {
        return fail(msg, null);
    }

    public BootstrapException fail(Throwable cause) {
        return fail(null, cause);
    }

    public synchronized BootstrapException fail(String msg, Throwable cause) {
        assembly.remove(this);
        StringBuilder buf = new StringBuilder();
        buf.append("Error loading bundle ").append(id).append(" (")
                .append(source == null ? "unknown source" : source).append(")");
        if ( msg != null ) {
            buf.append(": ").append(msg);
            if ( cause != null ) {
                buf.append(" (").append(cause).append(")");
            }
        }
        else if ( cause != null ) {
            buf.append(": ").append(cause);
        }
        return new BootstrapException(buf.toString(), cause);
    }

}
