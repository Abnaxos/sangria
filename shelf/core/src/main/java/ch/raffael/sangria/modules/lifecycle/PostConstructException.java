package ch.raffael.sangria.modules.lifecycle;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PostConstructException extends RuntimeException {

    public PostConstructException() {
        super();
    }

    public PostConstructException(String message) {
        super(message);
    }

    public PostConstructException(Throwable cause) {
        super(cause);
    }

    public PostConstructException(String message, Throwable cause) {
        super(message, cause);
    }
}
