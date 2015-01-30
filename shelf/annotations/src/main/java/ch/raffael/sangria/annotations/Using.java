package ch.raffael.sangria.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.sangria.annotations.index.Indexed;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
@Documented
@Indexed
@Repeatable(Using.List.class)
public @interface Using {

    String value();

    @Target({ ElementType.PACKAGE, ElementType.TYPE })
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @Indexed
    @interface List {

        Using[] value();
    }
}
