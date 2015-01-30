package ch.raffael.sangria.annotations.util;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Credit {

    String value();

}
