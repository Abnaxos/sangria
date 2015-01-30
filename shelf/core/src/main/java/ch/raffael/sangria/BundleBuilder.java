package ch.raffael.sangria;

import java.net.URI;
import java.net.URL;
import java.util.Set;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface BundleBuilder {

    URI getUri();

    BundleBuilder addToClassPath(URL url);
    BundleBuilder addToClassPath(URL... url);
    BundleBuilder addToClassPath(Iterable<URL> url);

    Set<URL> classPath();

}
