package ch.raffael.sangria.core;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.BindingAnnotation;

import ch.raffael.sangria.annotations.Default;
import ch.raffael.sangria.annotations.EnvValue;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindingAnnotation
@EnvValue
@Default(".2c-1.0c")
public @interface ApplicationEventsParallelism {
}
