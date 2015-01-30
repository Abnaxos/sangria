package ch.raffael.sangria.util.bind;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import ch.raffael.sangria.libs.guava.base.Objects;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractExtensionPoint {

    private Binder binder;
    private Map<Key, Object> multibinders;

    @Inject
    protected AbstractExtensionPoint(Binder binder) {
        Class<?> skip = getClass();
        while ( true ) {
            binder.skipSources(skip);
            if ( skip.equals(AbstractExtensionPoint.class) ) {
                break;
            }
            skip = skip.getSuperclass();
        }
        this.binder = binder;
    }

    protected Binder binder() {
        return binder;
    }

    protected Binder binder(Binder binder) {
        this.binder = binder;
        return binder;
    }

    @SuppressWarnings("unchecked")
    protected <T> Multibinder<T> multibinder(Class<T> type) {
        initMultibinders();
        Key key = new Key(Multibinder.class, type);
        Multibinder<T> multibinder = (Multibinder<T>)multibinders.get(key);
        if ( multibinder == null ) {
            multibinder = Multibinder.newSetBinder(binder, type);
            multibinders.put(key, multibinder);
        }
        return multibinder;
    }

    @SuppressWarnings("unchecked")
    protected <T> Multibinder<T> multibinder(Class<T> type, Annotation annotation) {
        initMultibinders();
        Key key = new Key(Multibinder.class, type, annotation);
        Multibinder<T> multibinder = (Multibinder<T>)multibinders.get(key);
        if ( multibinder == null ) {
            multibinder = Multibinder.newSetBinder(binder, type, annotation);
            multibinders.put(key, multibinder);
        }
        return multibinder;
    }

    @SuppressWarnings("unchecked")
    protected <T> Multibinder<T> multibinder(Class<T> type, Class<? extends Annotation> annotationType) {
        initMultibinders();
        Key key = new Key(Multibinder.class, type, annotationType);
        Multibinder<T> multibinder = (Multibinder<T>)multibinders.get(key);
        if ( multibinder == null ) {
            multibinder = Multibinder.newSetBinder(binder, type, annotationType);
            multibinders.put(key, multibinder);
        }
        return multibinder;
    }

    @SuppressWarnings("unchecked")
    protected <T> Multibinder<T> multibinder(TypeLiteral<T> type) {
        initMultibinders();
        Key key = new Key(Multibinder.class, type);
        Multibinder<T> multibinder = (Multibinder<T>)multibinders.get(key);
        if ( multibinder == null ) {
            multibinder = Multibinder.newSetBinder(binder, type);
            multibinders.put(key, multibinder);
        }
        return multibinder;
    }

    @SuppressWarnings("unchecked")
    protected <T> Multibinder<T> multibinder(TypeLiteral<T> type, Annotation annotation) {
        initMultibinders();
        Key key = new Key(Multibinder.class, type, annotation);
        Multibinder<T> multibinder = (Multibinder<T>)multibinders.get(key);
        if ( multibinder == null ) {
            multibinder = Multibinder.newSetBinder(binder, type, annotation);
            multibinders.put(key, multibinder);
        }
        return multibinder;
    }

    @SuppressWarnings("unchecked")
    protected <T> Multibinder<T> multibinder(TypeLiteral<T> type, Class<? extends Annotation> annotationType) {
        initMultibinders();
        Key key = new Key(Multibinder.class, type, annotationType);
        Multibinder<T> multibinder = (Multibinder<T>)multibinders.get(key);
        if ( multibinder == null ) {
            multibinder = Multibinder.newSetBinder(binder, type, annotationType);
            multibinders.put(key, multibinder);
        }
        return multibinder;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> MapBinder<K, V> mapBinder(Class<K> keyType, Class<V> valueType) {
        initMultibinders();
        Key key = new Key(MapBinder.class, keyType, valueType);
        MapBinder<K, V> mapBinder = (MapBinder<K, V>)multibinders.get(key);
        if ( mapBinder == null ) {
            mapBinder = MapBinder.newMapBinder(binder, keyType, valueType);
            multibinders.put(key, mapBinder);
        }
        return mapBinder;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> MapBinder<K, V> mapBinder(Class<K> keyType, Class<V> valueType, Annotation annotation) {
        initMultibinders();
        Key key = new Key(MapBinder.class, keyType, valueType, annotation);
        MapBinder<K, V> mapBinder = (MapBinder<K, V>)multibinders.get(key);
        if ( mapBinder == null ) {
            mapBinder = MapBinder.newMapBinder(binder, keyType, valueType, annotation);
            multibinders.put(key, mapBinder);
        }
        return mapBinder;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> MapBinder<K, V> mapBinder(Class<K> keyType, Class<V> valueType, Class<? extends Annotation> annotationType) {
        initMultibinders();
        Key key = new Key(MapBinder.class, keyType, valueType, annotationType);
        MapBinder<K, V> mapBinder = (MapBinder<K, V>)multibinders.get(key);
        if ( mapBinder == null ) {
            mapBinder = MapBinder.newMapBinder(binder, keyType, valueType, annotationType);
            multibinders.put(key, mapBinder);
        }
        return mapBinder;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> MapBinder<K, V> mapBinder(TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
        initMultibinders();
        Key key = new Key(MapBinder.class, keyType, valueType);
        MapBinder<K, V> mapBinder = (MapBinder<K, V>)multibinders.get(key);
        if ( mapBinder == null ) {
            mapBinder = MapBinder.newMapBinder(binder, keyType, valueType);
            multibinders.put(key, mapBinder);
        }
        return mapBinder;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> MapBinder<K, V> mapBinder(TypeLiteral<K> keyType, TypeLiteral<V> valueType, Annotation annotation) {
        initMultibinders();
        Key key = new Key(MapBinder.class, keyType, valueType, annotation);
        MapBinder<K, V> mapBinder = (MapBinder<K, V>)multibinders.get(key);
        if ( mapBinder == null ) {
            mapBinder = MapBinder.newMapBinder(binder, keyType, valueType, annotation);
            multibinders.put(key, mapBinder);
        }
        return mapBinder;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> MapBinder<K, V> mapBinder(TypeLiteral<K> keyType, TypeLiteral<V> valueType, Class<? extends Annotation> annotationType) {
        initMultibinders();
        Key key = new Key(MapBinder.class, keyType, valueType, annotationType);
        MapBinder<K, V> mapBinder = (MapBinder<K, V>)multibinders.get(key);
        if ( mapBinder == null ) {
            mapBinder = MapBinder.newMapBinder(binder, keyType, valueType, annotationType);
            multibinders.put(key, mapBinder);
        }
        return mapBinder;
    }

    private void initMultibinders() {
        if ( multibinders != null ) {
            multibinders = new HashMap<>();
        }
    }

    private static final class Key {
        private final Object[] values;
        private Key(Object... values) {
            this.values = values;
        }
        @Override
        public String toString() {
            return Objects.toStringHelper(this).addValue(Arrays.toString(values)).toString();
        }
        @Override
        public int hashCode() {
            return Objects.hashCode(values);
        }
        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null || getClass() != obj.getClass() ) {
                return false;
            }
            return Arrays.equals(this.values, ((Key)obj).values);
        }
    }

}
