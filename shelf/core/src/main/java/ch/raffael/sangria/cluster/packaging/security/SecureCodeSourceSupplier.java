package ch.raffael.sangria.cluster.packaging.security;

import java.net.URL;
import java.security.CodeSource;

import ch.raffael.sangria.cluster.packaging.CodeSourceSupplier;
import ch.raffael.sangria.cluster.packaging.ResourceResolver;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class SecureCodeSourceSupplier extends CodeSourceSupplier {

    private final ResourceResolver resourceResolver;

    public SecureCodeSourceSupplier(URL location, ResourceResolver resourceResolver) {
        super(location);
        this.resourceResolver = resourceResolver;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    @Override
    protected Object verify() {
        // FIXME: Not implemented
        return super.verify();
    }
}
