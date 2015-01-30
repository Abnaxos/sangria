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

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collector;

import ch.raffael.sangria.libs.guava.collect.ImmutableBiMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.collect.ImmutableListMultimap;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.ImmutableSetMultimap;
import ch.raffael.sangria.libs.guava.collect.ImmutableSortedMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableSortedSet;
import ch.raffael.sangria.libs.guava.collect.Ordering;


/**
 * Some collectors to collect the result of some stream calculation into Guava's collections.
 *
 * @todo Does not support all Guava collections yet.
 * @todo Consider supporting the original Guava classes (`com.googl.common.collections.*`)
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class GuavaCollectors {

    private GuavaCollectors() {
    }

    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
        return Collector.<T, ImmutableList.Builder<T>, ImmutableList<T>>of(
                ImmutableList::<T>builder,
                ImmutableList.Builder<T>::add,
                (a, b) -> a.addAll(b.build()),
                ImmutableList.Builder::build);
    }

    public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
        return Collector.<T, ImmutableSet.Builder<T>, ImmutableSet<T>>of(
                ImmutableSet::<T>builder,
                ImmutableSet.Builder<T>::add,
                (a, b) -> a.addAll(b.build()),
                ImmutableSet.Builder::build);
    }

    public static <T> Collector<T, ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> toImmutableSortedSet(Comparator<T> ordering) {
        return Collector.<T, ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>>of(
                () -> ImmutableSortedSet.orderedBy(ordering),
                ImmutableSortedSet.Builder<T>::add,
                (a, b) -> a.addAll(b.build()),
                ImmutableSortedSet.Builder::build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T extends Comparable> Collector<T, ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> toImmutableNaturallySortedSet() {
        return toImmutableSortedSet(Ordering.<T>natural());
    }

    public static <T extends Comparable> Collector<T, ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> toImmutableReverseSortedSet() {
        return toImmutableSortedSet(Ordering.<T>natural().reversed());
    }

    public static <T, K> Collector<T, ImmutableMap.Builder<K, T>, ImmutableMap<K, T>> toImmutableMap(Function<? super T, ? extends K> keyFunction) {
        return Collector.<T, ImmutableMap.Builder<K, T>, ImmutableMap<K, T>>of(
                ImmutableMap::builder,
                (builder, value) -> builder.put(keyFunction.apply(value), value),
                (a, b) -> a.putAll(b.build()),
                ImmutableMap.Builder::build);
    }

    public static <T, K> Collector<T, ImmutableSortedMap.Builder<K, T>, ImmutableSortedMap<K, T>> toImmutableSortedMap(Function<? super T, ? extends K> keyFunction, Comparator<K> ordering) {
        return Collector.<T, ImmutableSortedMap.Builder<K, T>, ImmutableSortedMap<K, T>>of(
                () -> ImmutableSortedMap.orderedBy(ordering),
                (builder, value) -> builder.put(keyFunction.apply(value), value),
                (a, b) -> a.putAll(b.build()),
                ImmutableSortedMap.Builder::build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T, K extends Comparable> Collector<T, ImmutableSortedMap.Builder<K, T>, ImmutableSortedMap<K, T>> toImmutableNaturallySortedMap(Function<? super T, ? extends K> keyFunction) {
        return toImmutableSortedMap(keyFunction, Ordering.<K>natural());
    }

    public static <T, K extends Comparable> Collector<T, ImmutableSortedMap.Builder<K, T>, ImmutableSortedMap<K, T>> toImmutableReverseSortedMap(Function<? super T, ? extends K> keyFunction) {
        return toImmutableSortedMap(keyFunction, Ordering.<K>natural().reversed());
    }

    public static <T, K> Collector<T, ImmutableBiMap.Builder<K, T>, ImmutableBiMap<K, T>> toImmutableBiMap(Function<? super T, ? extends K> keyFunction) {
        return Collector.<T, ImmutableBiMap.Builder<K, T>, ImmutableBiMap<K, T>>of(
                ImmutableBiMap::builder,
                (builder, value) -> builder.put(keyFunction.apply(value), value),
                (a, b) -> a.putAll(b.build()),
                ImmutableBiMap.Builder::build);
    }

    public static <T, K> Collector<T, ImmutableSetMultimap.Builder<K, T>, ImmutableSetMultimap<K, T>> toImmutableSetMultimap(Function<? super T, ? extends K> keyFunction) {
        return Collector.<T, ImmutableSetMultimap.Builder<K, T>, ImmutableSetMultimap<K, T>>of(
                ImmutableSetMultimap::builder,
                (builder, value) -> builder.put(keyFunction.apply(value), value),
                (a, b) -> ImmutableSetMultimap.<K, T>builder().putAll(a.build()).putAll(b.build()),
                ImmutableSetMultimap.Builder::build);
    }

    public static <T, K> Collector<T, ImmutableListMultimap.Builder<K, T>, ImmutableListMultimap<K, T>> toImmutableListMultimap(Function<? super T, ? extends K> keyFunction) {
        return Collector.<T, ImmutableListMultimap.Builder<K, T>, ImmutableListMultimap<K, T>>of(
                ImmutableListMultimap::builder,
                (builder, value) -> builder.put(keyFunction.apply(value), value),
                (a, b) -> a.putAll(b.build()),
                ImmutableListMultimap.Builder::build);
    }

}

