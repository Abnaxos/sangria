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

package ch.raffael.sangria.commons;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import ch.raffael.sangria.commons.annotations.development.Questionable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Questionable("Name: This was meant as replacement for Guava's Suppliers class for Java8; " +
        "however, most of that stuff es very easy to write using Lambdas " +
        "=> rename this class to `Lazy`?")
public class Suppliers {

    public static <T> Supplier<T> lazy(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Supplier<T> locklessLazy(Supplier<T> supplier) {
        return new LocklessLazy<>(supplier);
    }

    public static <T> Supplier<T> transientLazy(Supplier<T> supplier) {
        return new TransientLazy<>(supplier);
    }

    public static <T> Supplier<T> locklessTransientLazy(Supplier<T> supplier) {
        return new LocklessTransientLazy<>(supplier);
    }

    public static <T> Supplier<T> ofInstance(T instance) {
        return () -> instance;
    }

    private static class Lazy<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 20014030801L;

        private final Object lock;
        private final Supplier<T> supplier;
        private volatile Optional<T> value;

        public Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
            lock = new Object();
        }

        @Override
        public T get() {
            if ( value == null ) {
                synchronized ( lock ) {
                    if ( value == null ) {
                        value = Optional.ofNullable(supplier.get());
                    }
                }
            }
            return value.orElse(null);
        }
    }

    private static class LocklessLazy<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 20014030801L;

        private final AtomicReference<Optional<T>> value;
        private final Supplier<T> supplier;

        public LocklessLazy(Supplier<T> supplier) {
            this.supplier = supplier;
            value = new AtomicReference<>();
        }

        @Override
        public T get() {
            Optional<T> result = value.get();
            if ( result == null ) {
                result = Optional.ofNullable(supplier.get());
                if ( !value.compareAndSet(null, result) ) {
                    result = value.get();
                }
            }
            return result.orElse(null);
        }
    }

    private static class TransientLazy<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 20014030801L;

        private transient final Object lock;
        private final Supplier<T> supplier;

        private transient volatile Optional<T> value;

        public TransientLazy(Supplier<T> supplier) {
            this.supplier = supplier;
            lock = new Object();
        }
        @Override
        public T get() {
            if ( value == null ) {
                synchronized ( lock ) {
                    if ( value == null ) {
                        value = Optional.ofNullable(supplier.get());
                    }
                }
            }
            return value.orElse(null);
        }

    }

    private static class LocklessTransientLazy<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 20014030801L;

        private transient final AtomicReference<Optional<T>> value;
        private final Supplier<T> supplier;

        public LocklessTransientLazy(Supplier<T> supplier) {
            this.supplier = supplier;
            value = new AtomicReference<>();
        }

        @Override
        public T get() {
            Optional<T> result = value.get();
            if ( result == null ) {
                result = Optional.ofNullable(supplier.get());
                if ( !value.compareAndSet(null, result) ) {
                    result = value.get();
                }
            }
            return result.orElse(null);
        }
    }
}
