package ch.raffael.sangria.cluster.packaging;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class LocationUrls {

    public URL jarUrl(File jarFile) throws MalformedURLException {
        return new URL("jar:" + jarFile.toURI() + "!/");
    }

    public URL jarUrl(Path jarPath) throws MalformedURLException {
        return new URL("jar:" + jarPath.toUri() + "!/");
    }

}
