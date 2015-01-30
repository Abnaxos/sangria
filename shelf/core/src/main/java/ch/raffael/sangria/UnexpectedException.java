package ch.raffael.sangria;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class UnexpectedException extends RuntimeException {

    public UnexpectedException(Throwable cause) {
        this("Unexpected exception: " + cause, cause);
    }

    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
