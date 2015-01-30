package ch.raffael.sangria.modules.shutdown;

import com.google.inject.Binder;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.binder.LinkedBindingBuilder;

import ch.raffael.sangria.util.bind.AbstractExtensionPoint;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@ImplementedBy(ShutdownListenerBinder.Impl.class)
public interface ShutdownListenerBinder {

    LinkedBindingBuilder<ShutdownListener> bindShutdownListener();

    LinkedBindingBuilder<VetoableShutdownListener> bindVetoableShutdownListener();

    final class Impl extends AbstractExtensionPoint implements ShutdownListenerBinder {
        @Inject
        public Impl(Binder binder) {
            super(binder);
        }
        @Override
        public LinkedBindingBuilder<ShutdownListener> bindShutdownListener() {
            return multibinder(ShutdownListener.class).addBinding();
        }
        @Override
        public LinkedBindingBuilder<VetoableShutdownListener> bindVetoableShutdownListener() {
            return multibinder(VetoableShutdownListener.class).addBinding();
        }
    }

}
