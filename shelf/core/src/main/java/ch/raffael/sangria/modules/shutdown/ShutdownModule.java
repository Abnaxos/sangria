package ch.raffael.sangria.modules.shutdown;

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import ch.raffael.sangria.libs.guava.collect.ImmutableList;

import ch.raffael.sangria.ext.Completer;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ShutdownModule extends AbstractModule {

    private final String appId;
    private final Completer completer;
    private final List<Runnable> shutdownFinalizers;

    public ShutdownModule(String appId, Iterable<Runnable> shutdownFinalizers, Completer completer) {
        this.appId = appId;
        this.completer = completer;
        this.shutdownFinalizers = ImmutableList.copyOf(shutdownFinalizers);
        // FIXME: that won't work that way, but for now ...
    }

    @Override
    protected void configure() {
        bind(ShutdownCoordinator.class).toInstance(new ShutdownCoordinatorImpl(appId, shutdownFinalizers));
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                if ( type.getRawType().isInterface() || type.getRawType().isEnum() ) {
                    return;
                }

            }
        });
    }

    @Provides
    public ShutdownListenerBinder provideShutdown() {
        return completer.substantiate(ShutdownListenerBinder.class);
    }

}
