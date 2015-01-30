package ch.raffael.sangria.config;

import com.google.inject.Binder;
import com.google.inject.Provider;

import ch.raffael.sangria.libs.guava.base.Preconditions;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ConfigurationBinderProvider implements Provider<Binder> {

    private static final ThreadLocal<Binder> BINDER = new ThreadLocal<>();

    static void setBinder(Binder binder) {
        if ( binder == null ) {
            BINDER.remove();
        }
        BINDER.set(binder);
    }

    static Binder getBinder() {
        return BINDER.get();
    }

    @Override
    public Binder get() {
        Binder binder = BINDER.get();
        Preconditions.checkState(binder != null, "No binder set for current thread");
        return binder;
    }
}
