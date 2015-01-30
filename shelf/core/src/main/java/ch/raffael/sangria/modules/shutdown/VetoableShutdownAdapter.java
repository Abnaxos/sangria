package ch.raffael.sangria.modules.shutdown;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class VetoableShutdownAdapter implements VetoableShutdownListener {

    @Override
    public void shutdownRequested() throws ShutdownVetoException {
    }

    @Override
    public void shutdownVetoed(ShutdownVetoException veto) {
    }
}
