package ch.raffael.sangria.modules.lifecycle;

import com.google.inject.Binder;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.binder.LinkedBindingBuilder;

import ch.raffael.sangria.annotations.ExtensionPoint;
import ch.raffael.sangria.util.bind.AbstractExtensionPoint;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@ExtensionPoint
@ImplementedBy(LifecycleFacetBinder.Impl.class)
public interface LifecycleFacetBinder {

    LinkedBindingBuilder<LifecycleFacet> bindLifecycleFacet();

    final class Impl extends AbstractExtensionPoint implements LifecycleFacetBinder {
        @Inject
        public Impl(Binder binder) {
            super(binder);
        }
        @Override
        public LinkedBindingBuilder<LifecycleFacet> bindLifecycleFacet() {
            return multibinder(LifecycleFacet.class).addBinding();
        }
    }
}
