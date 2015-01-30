package ch.raffael.sangria.modules.lifecycle;

import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.inject.Inject;

import ch.raffael.sangria.libs.guava.collect.Lists;

import ch.raffael.sangria.modules.lifecycle.util.InvokeMethodActionSynthesizer;
import ch.raffael.sangria.dynamic.Reflection;

import static ch.raffael.sangria.dynamic.Reflection.Predicates.notOverridden;
import static ch.raffael.sangria.dynamic.Reflection.IterationMode.EXCLUDE_INTERFACES;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Jsr250Facet implements LifecycleFacet {

    private final InvokeMethodActionSynthesizer actionSynthesizer;

    @Inject
    Jsr250Facet(InvokeMethodActionSynthesizer actionSynthesizer) {
        this.actionSynthesizer = actionSynthesizer;
    }

    @Override
    public void examine(Class<?> type, Lifecycle lifecycle) {
        Lists.reverse(Reflection.allMethods(type, EXCLUDE_INTERFACES).stream()
                              .filter(notOverridden())
                              .filter(m -> m.getAnnotation(PostConstruct.class) != null)
                              .map(actionSynthesizer::actionFor)
                              .collect(Collectors.toCollection(LinkedList::new)))
                .forEach(lifecycle::onPostConstruct);
        Reflection.allMethods(type, EXCLUDE_INTERFACES).stream()
                .filter(notOverridden())
                .filter(m -> m.getAnnotation(PreDestroy.class) != null)
                .map(actionSynthesizer::actionFor)
                .forEach(lifecycle::onPreDestroy);
    }

}
