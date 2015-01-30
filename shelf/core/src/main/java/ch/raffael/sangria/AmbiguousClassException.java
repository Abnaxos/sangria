package ch.raffael.sangria;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AmbiguousClassException extends RuntimeException {

    public AmbiguousClassException() {
        super();
    }

    public AmbiguousClassException(String message) {
        super(message);
    }

    public AmbiguousClassException(Throwable cause) {
        super(cause);
    }

    public AmbiguousClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
