package ch.raffael.sangria.modules.shutdown;

import java.util.concurrent.Future;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ShutdownCoordinator {

    boolean isVetoable();

    Future<ShutdownVetoException> shutdown();

    Future<Void> forceShutdown();

    void addShutdownListener(ShutdownListener listener);

    void removeShutdownListener(ShutdownListener listener);

    void addVetoableShutdownListener(VetoableShutdownListener listener);

    void removeVetoableShutdownListener(VetoableShutdownListener listener);
}
