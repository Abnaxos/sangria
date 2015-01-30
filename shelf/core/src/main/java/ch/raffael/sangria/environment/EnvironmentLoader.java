package ch.raffael.sangria.environment;

import java.io.IOException;
import java.net.URI;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface EnvironmentLoader {

    boolean loadEnvironment(URI uri, EnvironmentBuilder envBuilder) throws IOException;

}
