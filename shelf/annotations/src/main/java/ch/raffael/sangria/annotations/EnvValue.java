package ch.raffael.sangria.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

import ch.raffael.sangria.annotations.index.Indexed;


/**
 * Annotation used to mark binding annotations as environment values. An example:
 *
 * ```java
 *
 * {@literal @}Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 * {@literal @}Documented
 * {@literal @}EnvValue("http.port")
 * {@literal @}Default("8080")
 * {@literal @}BindingAnnotation
 * public @interface HttpPort {
 *
 * }
 *
 * public class MyHttpServer {
 *     {@literal @}HttpPort
 *     private int port;
 * }
 * ```
 *
 * This will register that there is an environment value "http.port" and will bind it
 * as follows:
 *
 * ```java
 * bindConstant().annotatedWith(HttpPort.class).to("the value found in the environment");
 * ```
 *
 * If no such value is found in the environment, the `@Default` value will be used, if
 * specified, if no default value is specified, an error will be reported.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface EnvValue {

    /**
     * The name of the environment value. If no name is specified or the name is the
     * empty string, the fully qualified name of the annotated class will be used.
     */
    String name() default "";

    Class<? extends Function<String, String>> processor() default NoProcessor.class;

    final class NoProcessor implements Function<String, String> {
        @Override
        public String apply(String s) {
            return s;
        }
    }


}
