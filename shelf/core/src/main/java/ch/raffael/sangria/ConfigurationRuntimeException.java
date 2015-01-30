package ch.raffael.sangria;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ConfigurationRuntimeException extends RuntimeException {

    public ConfigurationRuntimeException() {
        super();
    }

    public ConfigurationRuntimeException(String message) {
        super(message);
    }

    public ConfigurationRuntimeException(Throwable cause) {
        super(cause);
    }

    public ConfigurationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
