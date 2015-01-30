package ch.raffael.sangria.assembly;

import org.slf4j.Logger;
import org.slf4j.Marker;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class MessageLogger extends AbstractMessager {

    private final Marker marker;
    private final Logger logger;

    public MessageLogger(Logger logger) {
        this(null, logger);
    }

    public MessageLogger(Marker marker, Logger logger) {
        this.marker = marker;
        this.logger = logger;
    }

    @Override
    public void error(Source source, String message, Throwable cause) {
        logger.error(source + ": " + message, cause);
    }

    @Override
    public void warning(Source source, String message, Throwable cause) {
        logger.warn(source + ": " + message, cause);
    }
}
