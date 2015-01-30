package ch.raffael.sangria.modules.shutdown;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ShutdownVetoException extends Exception {

    public ShutdownVetoException() {
        super();
    }

    public ShutdownVetoException(String message) {
        super(message);
    }

    public ShutdownVetoException(Throwable cause) {
        super(cause);
    }

    public ShutdownVetoException(String message, Throwable cause) {
        super(message, cause);
    }
}
