package ch.raffael.sangria;

import java.io.IOException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class LoadException extends IOException {

    public LoadException() {
    }

    public LoadException(String message) {
        super(message);
    }

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadException(Throwable cause) {
        super(cause);
    }
}
