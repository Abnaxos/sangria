package ch.raffael.sangria.assembly;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class DuplicateBundleException extends AssemblyException {

    private final Bundle bundle;


    public DuplicateBundleException(Bundle bundle) {
        super("Duplicate bundle at " + bundle.getUri());
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
