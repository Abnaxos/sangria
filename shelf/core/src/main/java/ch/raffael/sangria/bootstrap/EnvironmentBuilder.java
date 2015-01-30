package ch.raffael.sangria.bootstrap;

import java.lang.annotation.Annotation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface EnvironmentBuilder {

    EnvironmentBuilder set(String key, String value);

    EnvironmentBuilder set(Class<? extends Annotation> key, String value);

    EnvironmentBuilder remove(String key);

    EnvironmentBuilder remove(Class<? extends Annotation> key);

}
