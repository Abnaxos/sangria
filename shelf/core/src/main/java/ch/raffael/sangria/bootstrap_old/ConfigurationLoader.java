package ch.raffael.sangria.bootstrap_old;

import java.io.IOException;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ConfigurationLoader {

    void loadConfiguration(@NotNull Receiver receiver) throws IOException;

    interface Receiver {
        void set(@NotNull String key, @NotNull String value);
    }

}
