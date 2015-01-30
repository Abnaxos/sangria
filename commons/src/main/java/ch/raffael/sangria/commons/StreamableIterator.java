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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ch.raffael.sangria.commons.annotations.development.Questionable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Questionable("Try to avoid overloading `commons`")
public interface StreamableIterator<T> extends Iterator<T> {

    Stream<T> stream();

    static <T> Builder<T> builder(Iterator<T> iterator) {
        return new Builder<>(iterator);
    }

    final class Builder<T> {

        private final Iterator<T> iterator;
        private int characteristics;
        private boolean parallel;

        private Builder(Iterator<T> iterator) {
            this(iterator, 0, false);
        }

        private Builder(Iterator<T> iterator, int characteristics, boolean parallel) {
            this.iterator = iterator;
            this.characteristics = characteristics;
            this.parallel = parallel;
        }

        public Stream<T> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), parallel);
        }

        public StreamableIterator<T> wrapper() {
            class Wrapper implements StreamableIterator<T> {
                public boolean hasNext() {
                    return iterator.hasNext();
                }
                public T next() {
                    return iterator.next();
                }
                public void remove() {
                    iterator.remove();
                }
                public void forEachRemaining(Consumer<? super T> action) {
                    iterator.forEachRemaining(action);
                }
                @Override
                public Stream<T> stream() {
                    return Builder.this.stream();
                }
            }
            return new Wrapper();
        }

        /**
         * @return this
         * @see java.util.Spliterator#ORDERED
         */
        public Builder<T> ordered() {
            return ordered(true);
        }

        /**
         * @return this
         * @see java.util.Spliterator#ORDERED
         */
        public Builder<T> ordered(boolean flag) {
            return set(Spliterator.ORDERED, flag);
        }

        /**
         * @return this
         * @see java.util.Spliterator#DISTINCT
         */
        public Builder<T> distinct() {
            return distinct(true);
        }

        /**
         * @return this
         * @see java.util.Spliterator#DISTINCT
         */
        public Builder<T> distinct(boolean flag) {
            return set(Spliterator.DISTINCT, flag);
        }

        /**
         * @return this
         * @see java.util.Spliterator#SORTED
         */
        public Builder<T> sorted() {
            return sorted(true);
        }

        /**
         * @return this
         * @see java.util.Spliterator#SORTED
         */
        public Builder<T> sorted(boolean flag) {
            return set(Spliterator.SORTED, flag);
        }

        // SIZED doesn't make any sense here
        //
        ///**
        // * @return this
        // * @see java.util.Spliterator#SIZED
        // */
        //public Builder<T> sized() {
        //    return sized(true);
        //}
        //
        ///**
        // * @return this
        // * @see java.util.Spliterator#SIZED
        // */
        //public Builder<T> sized(boolean flag) {
        //    return set(Spliterator.SIZED, flag);
        //}

        /**
         * @return this
         * @see java.util.Spliterator#NONNULL
         */
        public Builder<T> nonnull() {
            return nonnull(true);
        }

        /**
         * @return this
         * @see java.util.Spliterator#NONNULL
         */
        public Builder<T> nonnull(boolean flag) {
            return set(Spliterator.NONNULL, flag);
        }

        /**
         * @return this
         * @see java.util.Spliterator#IMMUTABLE
         */
        public Builder<T> immutable() {
            return immutable(true);
        }

        /**
         * @return this
         * @see java.util.Spliterator#IMMUTABLE
         */
        public Builder<T> immutable(boolean flag) {
            return set(Spliterator.IMMUTABLE, flag);
        }

        /**
         * @return this
         * @see java.util.Spliterator#CONCURRENT
         */
        public Builder<T> concurrent() {
            return concurrent(true);
        }

        /**
         * @return this
         * @see java.util.Spliterator#CONCURRENT
         */
        public Builder<T> concurrent(boolean flag) {
            return set(Spliterator.CONCURRENT, flag);
        }

        // SIZED doesn't make any sense here
        //
        ///**
        // * @return this
        // * @see java.util.Spliterator#SUBSIZED
        // */
        //public Builder<T> subsized() {
        //    return subsized(true);
        //}
        //
        ///**
        // * @return this
        // * @see java.util.Spliterator#SUBSIZED
        // */
        //public Builder<T> subsized(boolean flag) {
        //    return set(Spliterator.SUBSIZED, flag);
        //}

        public Builder<T> parallel() {
            return parallel(true);
        }

        private Builder<T> parallel(boolean flag) {
            parallel = flag;
            return this;
        }

        private Builder<T> set(int flag, boolean value) {
            if ( value ) {
                characteristics |= flag;
            }
            else {
                characteristics &= ~flag;
            }
            return this;
        }

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface Streamable {
        int characteristics() default 0;
        boolean parallel() default false;
    }

}
