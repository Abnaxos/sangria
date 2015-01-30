package ch.raffael.sangria.modules.lifecycle;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeListener;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface LifecycleFacet {

    void examine(Class<?> type, Lifecycle lifecycle);

}
