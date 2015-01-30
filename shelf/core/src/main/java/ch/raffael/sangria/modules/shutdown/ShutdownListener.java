package ch.raffael.sangria.modules.shutdown;

import java.util.EventListener;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ShutdownListener extends EventListener {

    void prepareShutdown();

    void performShutdown();

    void postShutdown();

}
