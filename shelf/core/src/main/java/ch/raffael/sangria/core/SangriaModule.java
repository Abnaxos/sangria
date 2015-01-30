package ch.raffael.sangria.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import ch.raffael.sangria.annotations.ConfigurationInjectable;
import ch.raffael.sangria.annotations.Phase;
import ch.raffael.sangria.dynamic.Reflection;
import ch.raffael.sangria.eventbus.EventBus;
import ch.raffael.sangria.util.values.Parallelism;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class SangriaModule extends AbstractModule {

    private final Phase phase;

    SangriaModule(Phase phase) {
        this.phase = phase;
    }

    @Override
    protected void configure() {
        bind(Phase.class).toInstance(phase);
        switch ( phase ) {
            case CONFIGURATION:
                bindListener(Matchers.any(), new TypeListener() {
                    @Override
                    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                        if ( Reflection.getAnnotationFromClassOrPackage(type.getRawType(), ConfigurationInjectable.class) == null ) {
                            encounter.addError("Type %s is not injectable during %s phase", type, phase);
                        }
                    }
                });
                break;
            case RUNTIME:
                bindListener(Matchers.any(), new TypeListener() {
                    @Override
                    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                        ConfigurationInjectable injectable = Reflection.getAnnotationFromClassOrPackage(type.getRawType(), ConfigurationInjectable.class);
                        if ( injectable != null && injectable.configurationOnly() ) {
                            encounter.addError("Type %s is not injectable during %s phase", type, phase);
                        }
                    }
                });
                break;
        }

        Parallelism.Converter.bind(binder());
    }

    @Provides
    @Singleton
    @ApplicationEvents
    EventBus applicationEventBus(@ApplicationEventsParallelism Parallelism parallelism) {
        return EventBus.newParallelEventBus()
                .corePoolSize(parallelism.getCoreThreads())
                .maxPoolSize(parallelism.getMaxThreads())
                .named(ApplicationEvents.class.getName())
                .build();
    }

}
