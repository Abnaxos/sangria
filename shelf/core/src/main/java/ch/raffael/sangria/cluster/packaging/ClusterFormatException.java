package ch.raffael.sangria.cluster.packaging;

import java.io.IOException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ClusterFormatException extends IOException {

    public ClusterFormatException() {
        super();
    }

    public ClusterFormatException(String message) {
        super(message);
    }

    public ClusterFormatException(Throwable cause) {
        super(cause);
    }

    public ClusterFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
