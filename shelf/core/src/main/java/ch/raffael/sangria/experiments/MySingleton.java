package ch.raffael.sangria.experiments;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class MySingleton implements Scope {

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        // FIXME: Not implemented

        return null;
    }
}
