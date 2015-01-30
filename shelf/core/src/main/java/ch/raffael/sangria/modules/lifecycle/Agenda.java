package ch.raffael.sangria.modules.lifecycle;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
final class Agenda {

    private static final Logger log = LoggerFactory.getLogger(Agenda.class);

    private final Set<Action> onPostConstruct;
    private final Set<Action> onPreDestroy;

    Agenda(Set<Action> onPostConstruct, Set<Action> onPreDestroy) {
        this.onPostConstruct = onPostConstruct;
        this.onPreDestroy = onPreDestroy;
    }

    boolean isEmpty() {
        return onPostConstruct.isEmpty() && onPreDestroy.isEmpty();
    }

    void postConstruct(Object target) {
        for ( Action action : onPostConstruct ) {
            try {
                action.perform(target);
            }
            catch ( AssertionError | Exception e ) {
                log.error("Error in post-construct action {} on {}", action, target, e);
                preDestroy(target);
                throw new PostConstructException("Error in post-construct action {} on {}: " + e, e);
            }
        }
    }

    void preDestroy(Object target) {
        for ( Action action : onPreDestroy ) {
            try {
                action.perform(target);
            }
            catch ( AssertionError | Exception e ) {
                log.error("Error in pre-destroy action {} on {}", action, target, e);
            }
        }
    }

}
