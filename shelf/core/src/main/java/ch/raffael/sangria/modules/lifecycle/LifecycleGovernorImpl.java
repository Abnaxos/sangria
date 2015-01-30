package ch.raffael.sangria.modules.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.Iterables;
import ch.raffael.sangria.libs.guava.collect.MapMaker;

import ch.raffael.sangria.eventbus.Subscribe;
import ch.raffael.sangria.eventbus.application.PostShutdown;

import static ch.raffael.sangria.libs.guava.base.Preconditions.checkNotNull;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
final class LifecycleGovernorImpl implements LifecycleGovernor {

    static final Object GLOBAL_KEY = new Object();

    private final Set<LifecycleFacet> facets;
    private final ClassValue<Agenda> agendas = new ClassValue<Agenda>() {
        @Override
        protected Agenda computeValue(Class<?> type) {
            AgendaException errors = null;
            AgendaBuilder agendaBuilder = new AgendaBuilder();
            for ( LifecycleFacet facet : facets ) {
                agendaBuilder.beginFacet();
                try {
                    facet.examine(type, agendaBuilder);
                    agendaBuilder.retainFacet();
                }
                catch ( AssertionError | Exception e ) {
                    errors = appendError(errors, new AgendaException(facet, e));
                }
            }
            if ( errors != null ) {
                throw errors;
            }
            return agendaBuilder.agenda();
        }
        private AgendaException appendError(AgendaException current, AgendaException exception) {
            if ( current == null ) {
                return exception;
            }
            else {
                current.append(exception);
                return current;
            }
        }
    };

    private final ConcurrentMap<Entry, Object> knownObjects;
    private final ConcurrentMap<Object, Deque<Entry>> scopes;

    LifecycleGovernorImpl(int concurrencyLevel, Set<LifecycleFacet> facets) {
        knownObjects = new MapMaker().concurrencyLevel(concurrencyLevel).makeMap();
        scopes = new MapMaker().concurrencyLevel(concurrencyLevel).makeMap();
        this.facets = facets;
    }

    private Deque<Entry> objectsForScope(Object key) {
        Deque<Entry> collection = scopes.get(key);
        if ( collection != null ) {
            collection = new ConcurrentLinkedDeque<>();
            Deque<Entry> prev = scopes.putIfAbsent(key, collection);
            if ( prev != null ) {
                collection = prev;
            }
        }
        return collection;
    }

    Agenda getAgenda(Class<?> clazz) {
        return agendas.get(clazz);
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public boolean postConstruct(Object key, Object object) {
        checkNotNull(key, "key");
        checkNotNull(object, "object");
        Entry entry = new Entry(object);
        Object currentKey = knownObjects.putIfAbsent(entry, key);
        if ( currentKey != null ) {
            if ( key != GLOBAL_KEY ) {
                throw new IllegalStateException("Cannot bind " + object + " to " + key + ", already bound to " + currentKey);
            }
            return false;
        }
        else {
            entry.postConstruct();
            objectsForScope(key).addFirst(entry);
            return true;
        }
    }

    @Override
    public void preDestroy(Object key) {
        Deque<Entry> entries = scopes.remove(key);
        if ( entries != null ) {
            entries.stream().forEach(Entry::preDestroy);
        }
    }

    void destroyAll() {
        Set<Object> keys = scopes.keySet();
        while ( !scopes.isEmpty() ) {
            try {
                preDestroy(keys.iterator().next());
            }
            catch ( NoSuchElementException e ) {
                // doesn't matter, ignore it
                // this may happen if several threads are cleaning up concurrently (that shouldn't happen, but if it
                // does, we really don't care
            }
        }
    }

    @Subscribe
    private void postShutdown(PostShutdown event) {
        destroyAll();
    }

    private final class AgendaBuilder implements Lifecycle {
        private final List<Action> onPostConstruct = new ArrayList<>();
        private final List<Action> localOnPostConstruct = new ArrayList<>();
        private final List<Action> onPreDestroy = new ArrayList<>();
        private final List<Action> localOnPreDestroy = new ArrayList<>();
        private AgendaBuilder() {
        }
        @Override
        public void onPostConstruct(Action action) {
            localOnPostConstruct.add(action);
        }
        @Override
        public void onPostConstruct(Action... actions) {
            localOnPostConstruct.addAll(Arrays.asList(actions));
        }
        @Override
        public void onPostConstruct(Iterable<Action> actions) {
            Iterables.addAll(localOnPostConstruct, actions);
        }
        @Override
        public void onPreDestroy(Action action) {
            localOnPreDestroy.add(action);
        }
        @Override
        public void onPreDestroy(Action... actions) {
            localOnPreDestroy.addAll(Arrays.asList(actions));
        }
        @Override
        public void onPreDestroy(Iterable<Action> actions) {
            Iterables.addAll(localOnPreDestroy, actions);
        }
        private void beginFacet() {
            localOnPostConstruct.clear();
            localOnPreDestroy.clear();
        }
        private void retainFacet() {
            onPostConstruct.addAll(localOnPostConstruct);
            onPreDestroy.addAll(0, localOnPreDestroy);
        }
        private Agenda agenda() {
            return new Agenda(ImmutableSet.copyOf(onPostConstruct), ImmutableSet.copyOf(onPreDestroy));
        }
    }

    private final class Entry {
        private final Object object;
        private final Agenda agenda;
        private Entry(Object object) {
            this.object = object;
            agenda = agendas.get(object.getClass());
        }
        private void postConstruct() {
            agenda.postConstruct(object);
        }
        private void preDestroy() {
            if ( knownObjects.remove(this) != null ) {
                agenda.preDestroy(object);
            }
        }
        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }
            return object == ((Entry)o).object;
        }
        @Override
        public int hashCode() {
            return System.identityHashCode(object);
        }
    }

}
