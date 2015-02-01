package ch.raffael.sangria.cluster;

import java.util.stream.Stream;

import ch.raffael.sangria.libs.guava.collect.Iterables;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ClusterFileSystem extends AutoCloseable {

    Attributes getAttributes();

    Iterable<ResourceResolver> resolvers();
    Stream<ResourceResolver> resolverStream();

    default Resource resource(String path) {
        return resolverStream().map(resolver -> resolver.getResource(path)).findFirst().orElse(null);
    }

    default Iterable<Resource> resources(String path) {
        return Iterables.filter(Iterables.concat(Iterables.<ResourceResolver, Iterable<Resource>>transform(resolvers(), ResourceResolver::resources)),
                                res -> res.getPath().equals(path));
    }

    default Stream<Resource> resourcesStream(String path) {
        return resolverStream().flatMap(ResourceResolver::resourceStream).filter(res -> res.getPath().equals(path));
    }

}
