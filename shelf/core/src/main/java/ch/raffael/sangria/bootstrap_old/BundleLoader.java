package ch.raffael.sangria.bootstrap_old;

import java.io.IOException;
import java.net.URI;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface BundleLoader {

    boolean loadBundles(URI uri) throws IOException;

}
