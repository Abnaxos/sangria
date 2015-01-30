package ch.raffael.sangria.environment;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Environment {

    String get(String key);

    String get(String key, String fallback);

    String get(Class<? extends Annotation> key);

    String get(Class<? extends Annotation> key, String fallback);

    Collection<Map.Entry<String, String>> entries();

}
