package ch.raffael.sangria.annotations.index;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to inject annotated elements from the index at configuration time.
 *
 * **Example:**
 *
 * ```java
 * public class MyModule extends AbstractModule {
 *     {@literal @}Inject
 *     {@literal @}AnnotatedWith(MyIndexedAnnotation.class)
 *     private Set<AnnotatedElement> myIndexedElements;
 * }
 * ```
 *
 * In this case, myIndexedElement will receive all packages or classes, that are annotated
 * with `MyIndexedAnnotation` (or classes that contain annotated methods, fields or
 * constructors, or classes that contain methods or constructors with annotated parameters
 * or local variables).
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnnotatedWith {

    Class<? extends Annotation> value();

}
