package ch.raffael.sangria.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.raffael.sangria.annotations.index.Indexed;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.CLASS)
@Documented
@Indexed
@Repeatable(Extends.List.class)
public @interface Extends {

    String value();

    @Target(ElementType.PACKAGE)
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @Indexed
    @interface List {
        Extends[] value();
    }

}
