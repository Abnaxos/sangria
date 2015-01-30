package ch.raffael.sangria.modules.lifecycle;

import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import ch.raffael.sangria.annotations.Install;
import ch.raffael.sangria.annotations.Phase;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Install(Phase.RUNTIME)
public abstract class LifecycleModule extends AbstractModule implements LifecycleFacetBinder {

    private final Object governorLock = new Object();
    private final int concurrencyLevel;
    private volatile LifecycleGovernorImpl governor = null;

    @Inject
    public LifecycleModule(@ConcurrencyLevel int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    @Override
    protected void configure() {
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                final LifecycleGovernorImpl governor = getGovernor(encounter);
                final Agenda agenda;
                try {
                    agenda = governor.getAgenda(type.getRawType());
                }
                catch ( AgendaException e ) {
                    for ( AgendaException error : e ) {
                        encounter.addError("Error applying lifecycle facet " + error.getFacet(), error.getCause());
                    }
                    return;
                }
                if ( !agenda.isEmpty() ) {
                    encounter.register((InjectionListener<I>)injectee -> governor.postConstruct(LifecycleGovernorImpl.GLOBAL_KEY, injectee));
                }
            }
            private <I> LifecycleGovernorImpl getGovernor(TypeEncounter<I> encounter) {
                LifecycleGovernorImpl governor = LifecycleModule.this.governor;
                if ( governor == null ) {
                    governor = createGovernor(encounter.getProvider(Key.get(new TypeLiteral<Set<LifecycleFacet>>() {
                    })).get());
                }
                return governor;
            }
        });
        bindLifecycleFacet().to(Jsr250Facet.class);
    }

    @Provides
    @Singleton
    public LifecycleGovernor getGovernor(Set<LifecycleFacet> facets) {
        if ( governor == null ) {
            createGovernor(facets);
        }
        return governor;
    }

    private LifecycleGovernorImpl createGovernor(Set<LifecycleFacet> facets) {
        synchronized ( governorLock ) {
            if ( governor == null ) {
                governor = new LifecycleGovernorImpl(concurrencyLevel, facets);
            }
        }
        return governor;
    }

}
