package ch.raffael.sangria.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.sangria.annotations.index.Indexed;
import ch.raffael.sangria.annotations.meta.ModuleAnnotation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
@Indexed
@ModuleAnnotation
public @interface Install {

    Phase[] value() default Phase.RUNTIME;

}
