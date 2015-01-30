package ch.raffael.sangria.modules.lifecycle;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Lifecycle {

    void onPostConstruct(Action action);

    default void onPostConstruct(Action... actions) {
        for ( Action action : actions ) {
            onPostConstruct(action);
        }
    }

    default void onPostConstruct(Iterable<Action> actions) {
        for ( Action action : actions ) {
            onPostConstruct(action);
        }
    }

    default void onPostConstruct(Collection<Action> actions) {
        onPostConstruct((Iterable<Action>)actions);
    }

    void onPreDestroy(Action action);

    default void onPreDestroy(Action... actions) {
        for ( Action action : actions ) {
            onPreDestroy(action);
        }
    }

    default void onPreDestroy(Iterable<Action> actions) {
        for ( Action action : actions ) {
            onPreDestroy(action);
        }
    }

    default void onPreDestroy(Collection<Action> actions) {
        onPreDestroy((Iterable<Action>)actions);
    }

}
