/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Raffael Herzog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ch.raffael.sangria.dynamic;

import java.lang.reflect.Member;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import ch.raffael.sangria.libs.guava.base.MoreObjects;
import ch.raffael.sangria.libs.guava.cache.Cache;
import ch.raffael.sangria.libs.guava.cache.CacheBuilder;

import ch.raffael.sangria.commons.UnreachableCodeError;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ClassCache<K, V> {

    private final BiFunction<Class<?>, K, V> loader;
    private final CacheBuilder<K, V> builder;

    private final ClassValue<Cache<K, V>> caches = new ClassValue<Cache<K, V>>() {
        @Override
        protected Cache<K, V> computeValue(Class<?> type) {
            return builder.build();
        }
    };

    ClassCache(BiFunction<Class<?>, K, V> loader, CacheBuilder<K, V> builder) {
        this.loader = loader;
        this.builder = builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public V get(Class<?> type, K key) {
        return get(type, key, this.loader);
    }

    public V get(Class<?> type, K key, BiFunction<Class<?>, K, V> loader) {
        try {
            return caches.get(type).get(key, () -> MoreObjects.firstNonNull(loader, this.loader).apply(type, key));
        }
        catch ( ExecutionException e ) {
            throw new UnreachableCodeError(e);
        }
    }

    public void put(Class<?> type, K key, V value) {
        caches.get(type).put(key, value);
    }

    Cache<K, V> getCache(Class<?> type) {
        return caches.get(type);
    }

    public static final class Builder {

        private Consumer<CacheBuilder<?, ?>> configurator = (b) -> {};

        public Builder softKeys() {
            configurator = configurator.andThen(CacheBuilder::softValues);
            return this;
        }

        public Builder weakKeys() {
            configurator = configurator.andThen(CacheBuilder::weakValues);
            return this;
        }

        public Builder softValues() {
            configurator = configurator.andThen(CacheBuilder::softValues);
            return this;
        }

        public Builder weakValues() {
            configurator = configurator.andThen(CacheBuilder::weakValues);
            return this;
        }

        public Builder concurrencyLevel(int concurrencyLevel) {
            configurator = configurator.andThen((b) -> b.concurrencyLevel(concurrencyLevel));
            return this;
        }

        public Builder configure(Consumer<CacheBuilder<?, ?>> consumer) {
            configurator = configurator.andThen(consumer);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <K, V> ClassCache<K, V> newClassCache(BiFunction<Class<?>, K, V> loader) {
            CacheBuilder builder = CacheBuilder.newBuilder();
            configurator.accept(builder);
            return new ClassCache<>(loader, builder);
        }

        @SuppressWarnings("unchecked")
        public <K extends Member, V> MemberCache<K, V> newMemberCache(Function<K, V> loader) {
            CacheBuilder builder = CacheBuilder.newBuilder();
            configurator.accept(builder);
            return new MemberCache<>(loader, builder);
        }

    }

}
