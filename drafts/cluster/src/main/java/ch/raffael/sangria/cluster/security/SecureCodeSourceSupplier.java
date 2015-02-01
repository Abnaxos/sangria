package ch.raffael.sangria.cluster.security;

import java.net.URL;

import ch.raffael.sangria.cluster.CodeSourceSupplier;
import ch.raffael.sangria.cluster.ResourceResolver;


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
