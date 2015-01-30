package ch.raffael.sangria.cluster.packaging;

import java.util.stream.Stream;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ResourceResolver {

    String PATH_SEPARATOR = "/";

    /**
     * A path that identifies a resolver within its ClusterFileSystem.
     * @return
     */
    String getId();

    Attributes getAttributes();

    ClusterFileSystem getFileSystem();

    Iterable<Resource> resources();

    Stream<Resource> resourceStream();

    Resource getResource(String path);

}
