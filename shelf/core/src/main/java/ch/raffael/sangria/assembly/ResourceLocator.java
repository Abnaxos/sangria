package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ResourceLocator {

    @Nullable
    URL getResource(String name);

    @Nullable
    Iterator<URL> getResources(String name) throws IOException;

}
