package ch.raffael.sangria.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import ch.raffael.sangria.ConfigurationRuntimeException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Injections {

    private Injections() {
    }

    public static Supplier<?> of(Injector injector, AnnotatedElement target, Type type) {
        Annotation annotation = findBindingAnnotation(target);
        if ( annotation == null && type instanceof Class && Injector.class.isAssignableFrom((Class<?>)type) ) {
            return () -> injector;
        }
        else {
            if ( annotation == null ) {
                return injector.getProvider(Key.get(type))::get;
            }
            else {
                return injector.getProvider(Key.get(type, annotation))::get;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> of(Injector injector, AnnotatedElement target, Class<T> type) {
        Annotation annotation = findBindingAnnotation(target);
        if ( annotation == null && Injector.class.isAssignableFrom(type) ) {
            return () -> (T)injector;
        }
        else {
            return injector.getProvider(Key.get(type, annotation))::get;
        }
    }

    public static Supplier<?> of(Injector injector, AnnotatedElement target, TypeLiteral<?> type) {
        Annotation annotation = findBindingAnnotation(target);
        if ( annotation == null && Injector.class.isAssignableFrom(type.getRawType()) ) {
            return () -> injector;
        }
        else {
            return injector.getProvider(Key.get(type, annotation))::get;
        }
    }

    public static Function<Injector, ?> of(AnnotatedElement target, Type type) {
        Annotation annotation = findBindingAnnotation(target);
        if ( annotation == null && type instanceof Class && Injector.class.isAssignableFrom((Class<?>)type) ) {
            return Function.identity();
        }
        else {
            return (Injector injector) -> injector.getProvider(Key.get(type, annotation)).get();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<Injector, T> of(AnnotatedElement target, Class<T> type) {
        Annotation annotation = findBindingAnnotation(target);
        if ( annotation == null && Injector.class.isAssignableFrom(type) ) {
            return (Function<Injector, T>)Function.<Injector>identity();
        }
        else {
            return (Injector injector) -> injector.getProvider(Key.get(type, annotation)).get();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<Injector, T> of(AnnotatedElement target, TypeLiteral<T> type) {
        Annotation annotation = findBindingAnnotation(target);
        if ( annotation == null && Injector.class.isAssignableFrom(type.getRawType()) ) {
            return (Function<Injector, T>)Function.<Injector>identity();
        }
        else {
            return (Injector injector) -> injector.getProvider(Key.get(type, annotation)).get();
        }
    }

    public static Annotation findBindingAnnotation(AnnotatedElement target) {
        Annotation[] annotations = target.getAnnotations();
        Annotation annotation = null;
        for ( Annotation a : annotations ) {
            if ( a.annotationType().getAnnotation(BindingAnnotation.class) != null ) {
                if ( annotation != null ) {
                    throw new ConfigurationRuntimeException(target + ": Several binding annotations found");
                }
                annotation = a;
            }
        }
        return annotation;
    }

}
