package ch.raffael.sangria.assembly;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AssemblyException extends RuntimeException {

    public AssemblyException() {
        super();
    }

    public AssemblyException(String message) {
        super(message);
    }

    public AssemblyException(Throwable cause) {
        super(cause);
    }

    public AssemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}
