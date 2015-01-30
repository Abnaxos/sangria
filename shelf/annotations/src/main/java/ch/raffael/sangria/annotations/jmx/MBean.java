package ch.raffael.sangria.annotations.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MBean {

    Class<?> mbeanInterface() default Void.class;

    Property[] name();

    @interface Property {

        String name();

        String value() default "";

        Class<? extends Function<?, String>> generator() default NoGenerator.class;

    }

}
