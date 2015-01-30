package ch.raffael.sangria.assembly;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AmbiguousPackageException extends AssemblyException {

    public AmbiguousPackageException() {
        super();
    }

    public AmbiguousPackageException(String message) {
        super(message);
    }

    public AmbiguousPackageException(Throwable cause) {
        super(cause);
    }

    public AmbiguousPackageException(String message, Throwable cause) {
        super(message, cause);
    }
}
