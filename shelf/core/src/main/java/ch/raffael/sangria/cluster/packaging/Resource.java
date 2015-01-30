package ch.raffael.sangria.cluster.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSource;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Resource {

    ResourceResolver getResolver();

    String getPath();

    InputStream openStream() throws IOException;

    Attributes getAttributes();

    CodeSource getCodeSource();

}
