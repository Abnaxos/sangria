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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import ch.raffael.sangria.commons.annotations.development.Questionable;


/**
 * [Curry](http://en.wikipedia.org/wiki/Currying) functional interfaces from
 * {@link java.util.function}.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Questionable("Writing such a short lambda shouldn't be a problem. It was useful once to curry a BiFunction to a function, using the first argument as a state; maybe provide something better there?")
public final class Curry {

    public static <F, V> Consumer<V> curry(F fixed, BiConsumer<F, V> consumer) {
        return arg -> consumer.accept(fixed, arg);
    }
    public static <F, V> Consumer<V> rcurry(F fixed, BiConsumer<V, F> consumer) {
        return arg -> consumer.accept(arg, fixed);
    }

    public static <F, V, R> Function<V, R> curry(F fixed, BiFunction<F, V, R> function) {
        return arg -> function.apply(fixed, arg);
    }
    public static <F, V, R> Function<V, R> rcurry(F fixed, BiFunction<V, F, R> function) {
        return arg -> function.apply(arg, fixed);
    }

    public static <F, V> Predicate<V> curry(F fixed, BiPredicate<F, V> predicate) {
        return arg -> predicate.test(fixed, arg);
    }
    public static <F, V> Predicate<V> rcurry(F fixed, BiPredicate<V, F> predicate) {
        return arg -> predicate.test(arg, fixed);
    }

    public static <T> UnaryOperator<T> curry(T fixed, BinaryOperator<T> operator) {
        return arg -> operator.apply(fixed, arg);
    }
    public static <T> UnaryOperator<T> rcurry(T fixed, BinaryOperator<T> operator) {
        return arg -> operator.apply(arg, fixed);
    }

    public static IntUnaryOperator curry(int fixed, IntBinaryOperator operator) {
        return arg -> operator.applyAsInt(fixed, arg);
    }
    public static IntUnaryOperator rcurry(int fixed, IntBinaryOperator operator) {
        return arg -> operator.applyAsInt(arg, fixed);
    }

    public static LongUnaryOperator curry(long fixed, LongBinaryOperator operator) {
        return arg -> operator.applyAsLong(fixed, arg);
    }
    public static LongUnaryOperator rcurry(long fixed, LongBinaryOperator operator) {
        return arg -> operator.applyAsLong(arg, fixed);
    }

    public static <F, V> ToIntFunction<V> curry(F fixed, ToIntBiFunction<F, V> function) {
        return arg -> function.applyAsInt(fixed, arg);
    }
    public static <F, V> ToIntFunction<V> rcurry(F fixed, ToIntBiFunction<V, F> function) {
        return arg -> function.applyAsInt(arg, fixed);
    }

    public static <F, V> ToLongFunction<V> curry(F fixed, ToLongBiFunction<F, V> function) {
        return arg -> function.applyAsLong(fixed, arg);
    }
    public static <F, V> ToLongFunction<V> rcurry(F fixed, ToLongBiFunction<V, F> function) {
        return arg -> function.applyAsLong(arg, fixed);
    }

    public static <F, V> ToDoubleFunction<V> curry(F fixed, ToDoubleBiFunction<F, V> function) {
        return arg -> function.applyAsDouble(fixed, arg);
    }
    public static <F, V> ToDoubleFunction<V> rcurry(F fixed, ToDoubleBiFunction<V, F> function) {
        return arg -> function.applyAsDouble(arg, fixed);
    }

    // the following probably don't make much sense
    //
    // They return functional constructs that don't take any parameters; i.e. basically just contant
    // values. So, why should anyone do that? There may perfectly be good reasons to do something
    // like this, however, I think these are rather rare cases and don't deserve such utility
    // functions.

    public static <T> Runnable curry(T fixed, Consumer<T> consumer) {
        return () -> consumer.accept(fixed);
    }
    public static <T1, T2> Runnable curry(T1 fixed1, T2 fixed2, BiConsumer<T1, T2> consumer) {
        return () -> consumer.accept(fixed1, fixed2);
    }
    public static Runnable curry(int fixed, IntConsumer consumer) {
        return () -> consumer.accept(fixed);
    }
    public static Runnable curry(long fixed, LongConsumer consumer) {
        return () -> consumer.accept(fixed);
    }

    public static <F, R> Supplier<R> curry(F fixed, Function<F, R> function) {
        return () -> function.apply(fixed);
    }
    public static <F1, F2, R> Supplier<R> curry(F1 fixed1, F2 fixed2, BiFunction<F1, F2, R> function) {
        return () -> function.apply(fixed1, fixed2);
    }
    public static <F, R> Supplier<R> curryFunction(F fixed, Function<F, R> function) {
        return () -> function.apply(fixed);
    }
    public static <F1, F2, R> Supplier<R> curryBiFunction(F1 fixed1, F2 fixed2, BiFunction<F1, F2, R> function) {
        return () -> function.apply(fixed1, fixed2);
    }

    public static <T> Supplier<T> curry(T fixed, UnaryOperator<T> function) {
        return () -> function.apply(fixed);
    }
    public static <T> Supplier<T> curry(T fixed1, T fixed2, BinaryOperator<T> function) {
        return () -> function.apply(fixed1, fixed2);
    }

    public static IntSupplier curry(int fixed, IntUnaryOperator function) {
        return () -> function.applyAsInt(fixed);
    }
    public static IntSupplier curry(int fixed1, int fixed2, IntBinaryOperator function) {
        return () -> function.applyAsInt(fixed1, fixed2);
    }

    public static LongSupplier curry(long fixed, LongUnaryOperator function) {
        return () -> function.applyAsLong(fixed);
    }
    public static LongSupplier curry(long fixed1, long fixed2, LongBinaryOperator function) {
        return () -> function.applyAsLong(fixed1, fixed2);
    }

    public static LongSupplier curry(int fixed, IntToLongFunction function) {
        return () -> function.applyAsLong(fixed);
    }
    public static DoubleSupplier curry(int fixed, IntToDoubleFunction function) {
        return () -> function.applyAsDouble(fixed);
    }

    public static IntSupplier curry(long fixed, LongToIntFunction function) {
        return () -> function.applyAsInt(fixed);
    }
    public static DoubleSupplier curry(long fixed, LongToDoubleFunction function) {
        return () -> function.applyAsDouble(fixed);
    }

    public <T> T with(T target, Consumer<? super T> consumer) {
        consumer.accept(target);
        return target;
    }

}
