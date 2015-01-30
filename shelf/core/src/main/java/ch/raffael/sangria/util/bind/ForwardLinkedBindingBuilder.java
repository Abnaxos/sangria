package ch.raffael.sangria.util.bind;


import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.util.Providers;

import ch.raffael.sangria.libs.guava.base.Objects;

import ch.raffael.sangria.dynamic.Annotations;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ForwardLinkedBindingBuilder<T> implements LinkedBindingBuilder<T> {

    private final Binder binder;

    private final SettableProvider<T> provider = new SettableProvider<>();

    public ForwardLinkedBindingBuilder(Binder binder) {
        this.binder = binder;
        binder.skipSources(ForwardLinkedBindingBuilder.class);
    }

    public Provider<T> provider() {
        return provider;
    }

    private <B extends T> LinkedBindingBuilder<B> bind(Key<B> key) {
        LinkedBindingBuilder<B> builder = binder.bind(key);
        provider.set(binder.getProvider(key));
        return builder;
    }

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        return bind(Key.get(implementation, Annotations.uniqueId()));
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        return bind(Key.get(implementation, Annotations.uniqueId()));
    }

    @Override
    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        return bind(targetKey);
    }

    @Override
    public void toInstance(T instance) {
        provider.set(Providers.of(instance));
    }

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> p) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toProvider(p);
    }

    @Override
    public ScopedBindingBuilder toProvider(javax.inject.Provider<? extends T> provider) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toProvider(provider);
    }

    @Override
    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toProvider(providerType);
    }

    @Override
    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toProvider(providerType);
    }

    @Override
    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toProvider(providerKey);
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toConstructor(constructor);
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
        return bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).toConstructor(constructor, type);
    }

    @Override
    public void in(Class<? extends Annotation> scopeAnnotation) {
        bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).in(scopeAnnotation);
    }

    @Override
    public void in(Scope scope) {
        bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).in(scope);
    }

    @Override
    public void asEagerSingleton() {
        bind(Key.get(new TypeLiteral<T>() {}, Annotations.uniqueId())).asEagerSingleton();
    }

    private static class SettableProvider<T> implements Provider<T>{

        private final AtomicReference<Provider<? extends T>> provider = new AtomicReference<>();

        @Override
        public String toString() {
            return "SettableProvider{"+provider.get()+"}";
        }

        @Override
        public boolean equals(Object o) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }
            SettableProvider that = (SettableProvider)o;
            return Objects.equal(provider.get(), that.provider.get());
        }

        @Override
        public int hashCode() {
            Provider<? extends T> p = provider.get();
            return p == null ? 0 : p.hashCode();
        }

        private void set(Provider<? extends T> provider) {
            if ( !this.provider.compareAndSet(null, provider) ) {
                throw new IllegalStateException("Already set");
            }
        }

        @Override
        public T get() {
            Provider<? extends T> provider = this.provider.get();
            if ( provider == null ) {
                throw new IllegalStateException("Not bound");
            }
            return provider.get();
        }
    }

}
