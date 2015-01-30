package ch.raffael.sangria.annotations.bindings;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindingAnnotation
public @interface AppInfo {

    Element value();

    enum Element {
        ID,
        SHORT_NAME,
        FULL_NAME,
        VERSION,

        VENDOR,
        URL,
        SUPPORT,

        COPYRIGHT,
        SHORT_LICENSE,
        FULL_LICENSE,

        CREDITS
    }

}
