package ch.raffael.sangria.bootstrap;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BootstrapException extends Exception {

    public BootstrapException() {
        super();
    }

    public BootstrapException(String message) {
        super(message);
    }

    public BootstrapException(Throwable cause) {
        super(cause);
    }

    public BootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
}
