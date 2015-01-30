package ch.raffael.sangria.modules.shutdown;

import java.util.EventListener;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface VetoableShutdownListener extends EventListener {

    void shutdownRequested() throws ShutdownVetoException;

    void shutdownVetoed(ShutdownVetoException veto);

}
