package ch.raffael.sangria.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Module;

import ch.raffael.sangria.annotations.meta.ModuleAnnotation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
@ModuleAnnotation
@Repeatable(DependsOn.List.class)
public @interface DependsOn {

    Class<? extends Module> value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @ModuleAnnotation
    @interface List {
        DependsOn[] value();
    }

}
