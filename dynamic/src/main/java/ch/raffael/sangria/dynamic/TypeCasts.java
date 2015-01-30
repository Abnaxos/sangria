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

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ch.raffael.sangria.libs.guava.base.Defaults;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;
import ch.raffael.sangria.libs.guava.primitives.Primitives;
import ch.raffael.sangria.libs.guava.reflect.TypeToken;

import ch.raffael.sangria.dynamic.asm.Label;
import ch.raffael.sangria.dynamic.asm.Type;
import ch.raffael.sangria.dynamic.asm.commons.GeneratorAdapter;

import static ch.raffael.sangria.libs.guava.primitives.Primitives.unwrap;
import static ch.raffael.sangria.libs.guava.primitives.Primitives.wrap;


/**
 * Utilities for performing type casts.
 *
 * Provides the following type casts:
 *
 *  primitive &rarr; primitive
 *  :   standard cast: `(int)12345L`
 *
 *  primitive &rarr; wrapper
 *  :   cast the value to the primitive type of the wrapper type, then wrap it: `Integer.valueOf((int)12345L)
 *
 *  wrapper &rarr; primitive
 *  :   unwrap using the `<type>Value()` method: `Long.valueOf(12345L).intValue()`
 *
 *  wrapper &rarr; wrapper
 *  :   unwrap the source type as target type and wrap again: `Integer.valueOf(Long.valueOf(12345L).intValue())`
 *
 *  void &rarr; anything
 *  :   push the default value (0, false, null)
 *
 *  anything &rarr; void
 *  :   discard the value
 *
 *  object &rarr; object
 *  :   Standard cast, possibly downcasting
 *
 *  If the cast is impossible (e.g. if source is `String` and target is `Object[]`), the method throws an
 *  {@link IncompatibleTypesException}. This also applies to boxed types (e.g. `int` &rarr; `String`).
 *
 * @todo When {@link Kind::DEFAULTS} is enabled, also provide conversions from and to {@link Number}
 *
 * @param source
 * @param target
 * @param gen
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ObjectEquality")
public final class TypeCasts {

    private static final EnumSet<Kind> DEFAULT_CASTS = EnumSet.of(Kind.SAFE);

    private static final CastMethod[] CAST_METHODS = CastMethod.values();

    private TypeCasts() {
    }

    /**
     * Generates the bytecode for casts.
     */
    public static void generateCast(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) throws IncompatibleTypesException {
        cast(kinds, source, target, (m, k) -> {
            m.generate(kinds, source, target, gen);
            return null;
        });
    }

    /**
     * Return a function that performs a cast dynamically.
     */
    public static <S, T> CastFunction<S, T> castFunction(Collection<Kind> kinds, TypeToken<S> source, TypeToken<T> target) throws IncompatibleTypesException {
        return cast(kinds, source, target, (m, k) -> new MethodCastFunction<>(k, EnumSet.copyOf(kinds), source, target, m));
    }

    /**
    * Perform a cast dynamically.
    */
    @SuppressWarnings("unchecked")
    public static <S, T> T performCast(Collection<Kind> kinds, TypeToken<S> source, TypeToken<T> target, S value) throws IncompatibleTypesException {
        if ( value != null && !source.isAssignableFrom(value.getClass()) ) {
            throw new IllegalArgumentException(value + " is not an instance of " + source);
        }
        return (T)cast(kinds, source, target, (m, k) -> m.perform(kinds, source, target, value));
    }

    /**
     * Get the {@link Kind} of a cast.
     */
    public static Kind getKind(TypeToken<?> source, TypeToken<?> target) {
        for ( CastMethod castMethod : CAST_METHODS ) {
            Kind kind = castMethod.kind(source, target);
            if ( kind != null ) {
                return kind;
            }
        }
        return null;
    }

    /**
     * Checks that a cast is one of the given kinds throwing an {@link IncompatibleTypesException} otherwise.
     */
    public static Kind ensureKind(EnumSet<Kind> kinds, TypeToken<?> source, TypeToken<?> target) throws IncompatibleTypesException {
        Kind kind = getKind(source, target);
        if ( kind == null ) {
            throw new IncompatibleTypesException(source, target, null);
        }
        if ( kind != Kind.SAFE && !kinds.contains(kind) ) {
            throw new IncompatibleTypesException(source, target, kind);
        }
        return kind;
    }

    private static <T> T cast(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Cast<T> cast) throws IncompatibleTypesException {
        for ( CastMethod castMethod : CAST_METHODS ) {
            Kind kind = castMethod.kind(source, target);
            if ( kind != null ) {
                if ( kind != Kind.SAFE && !kinds.contains(kind) ) {
                    throw new IncompatibleTypesException(source, target, kind);
                }
                return cast.cast(castMethod, kind);
            }
        }
        throw new IncompatibleTypesException(source, target, null);
    }

    private static boolean isWrapperType(Class<?> type) {
        if ( !Primitives.isWrapperType(type) ) {
            return false;
        }
        else if ( type == Void.class ) {
            // Void isn't a wrapper for us!
            return false;
        }
        else {
            return true;
        }
    }

    @FunctionalInterface
    private static interface Cast<T> {
        T cast(CastMethod Method, Kind kind) throws IncompatibleTypesException;
    }

    private static enum CastMethod {
        SIMPLE {
            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                if ( /*source.equals(target) ||*/ target.isAssignableFrom(source) ) {
                    return Kind.SAFE;
                }
                return null;
            }
            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                return value;
            }
            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) throws IncompatibleTypesException {
                if ( !target.isPrimitive() ) {
                    gen.checkCast(Type.getType(target.getRawType()));
                }
            }
        },
        VOID {
            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                if ( source.getRawType() == void.class || target.getRawType() == void.class ) {
                    return Kind.VOID;
                }
                return null;
            }

            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                if ( target.getRawType() == void.class ) {
                    return null;
                }
                else if ( source.getRawType() == void.class ) {
                    return Defaults.defaultValue(target.getRawType());
                }
                else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) {
                if ( target.getRawType() == void.class ) {
                    if ( source.getRawType() == long.class || source.getRawType() == double.class ) {
                        gen.pop2();
                    }
                    else {
                        gen.pop();
                    }
                }
                else if ( source.getRawType() == void.class ) {
                    if ( target.isPrimitive() ) {
                        if ( target.getRawType() == long.class ) {
                            gen.push(0L);
                        }
                        else if ( target.getRawType() == double.class ) {
                            gen.push(0d);
                        }
                        else if ( target.getRawType() == float.class ) {
                            gen.push(0f);
                        }
                        else {
                            // everything else can be pushed as int
                            gen.push(0);
                        }
                    }
                    else {
                        gen.push((String)null);
                    }
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
        },
        PRIM_PRIM {
            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                if ( source.isPrimitive() && target.isPrimitive() ) {
                    return primitiveKind(source.getRawType(), target.getRawType());
                }
                return null;
            }

            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                return WRAPPER_WRAPPER.perform(kinds, source.wrap(), target.wrap(), value);
            }
            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) {
                gen.cast(Type.getType(source.getRawType()), Type.getType(target.getRawType()));
            }
        },
        PRIM_WRAPPER {
            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                //if ( source.getRawType() == boolean.class && Boolean.class.isAssignableFrom(target.getRawType()) ) {
                //    return Kind.WIDENING;
                //}
                if ( source.isPrimitive() && !target.isPrimitive() ) {
                    if ( isWrapperType(target.getRawType()) ) {
                        return primitiveKind(source.getRawType(), unwrap(target.getRawType()));
                    }
                    else if ( target.isAssignableFrom(wrap(source.getRawType())) ) {
                        return Kind.SAFE;
                    }
                }
                return null;
            }

            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                return WRAPPER_WRAPPER.perform(kinds, source.wrap(), target, value);
            }

            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) throws IncompatibleTypesException {
                Class<?> primTarget = unwrap(target.getRawType());
                gen.cast(Type.getType(source.getRawType()), Type.getType(primTarget));
                gen.valueOf(Type.getType(primTarget));
            }
        },
        WRAPPER_PRIM {
            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                if ( isWrapperType(source.getRawType()) && target.isPrimitive() ) {
                    return primitiveKind(unwrap(source.getRawType()), target.getRawType());
                }
                return null;
            }

            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                value = WRAPPER_WRAPPER.perform(kinds, source.wrap(), target.wrap(), value);
                if ( value == null && kinds.contains(Kind.DEFAULTS) ) {
                    value = Defaults.defaultValue(unwrap(target.getRawType()));
                }
                return value;
            }

            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) throws IncompatibleTypesException {
                Label end = null;
                Label ifNonNull = gen.newLabel();
                gen.dup();
                gen.ifNonNull(ifNonNull);
                if ( kinds.contains(Kind.DEFAULTS) ) {
                    end = gen.newLabel();
                    gen.pop();
                    Object def = Defaults.defaultValue(target.getRawType());
                    if ( def instanceof Long ) {
                        gen.push((long)def);
                    }
                    else if ( def instanceof Double ) {
                        gen.push((double)def);
                    }
                    else if ( def instanceof Float ) {
                        gen.push((float)def);
                    }
                    else if ( def instanceof Boolean ) {
                        gen.push((boolean)def);
                    }
                    else {
                        gen.push((int)def);
                    }
                    gen.goTo(end);
                }
                else {
                    gen.throwException(Type.getType(NullPointerException.class), null);
                }
                gen.visitLabel(ifNonNull);
                Class<?> primSource = unwrap(source.getRawType());
                gen.unbox(Type.getType(source.getRawType()));
                gen.cast(Type.getType(primSource), Type.getType(target.getRawType()));
                if ( end != null ) {
                    gen.visitLabel(end);
                }
            }
        },
        WRAPPER_WRAPPER {
            private final Map<Class<?>, Function<Number, ?>> castFunctions =
                    ImmutableMap.<Class<?>, Function<Number, ?>>builder()
                            .put(Byte.class, Number::byteValue)
                            .put(Short.class, Number::shortValue)
                            .put(Integer.class, Number::intValue)
                            .put(Float.class, Number::floatValue)
                            .put(Long.class, Number::longValue)
                            .put(Double.class, Number::doubleValue)
                            .put(Character.class, (cp) -> (char)(int)cp)
                            .build();


            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                if ( isWrapperType(source.getRawType()) && isWrapperType(target.getRawType()) ) {
                    return primitiveKind(unwrap(source.getRawType()), unwrap(target.getRawType()));
                }
                return null;
            }

            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                if ( source.equals(target) ) {
                    // this may happen when (un)boxing things without conversion
                    return value;
                }
                if ( source.getRawType() == Character.class ) {
                    value = Integer.valueOf((Character)value);
                }
                else if ( source.getRawType() == Boolean.class ) {
                    return value;
                }
                return castFunctions.get(target.getRawType()).apply((Number)value);
            }

            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) {
                Label ifNull = gen.newLabel();
                gen.dup();
                gen.ifNull(ifNull);
                gen.unbox(Type.getType(source.getRawType()));
                gen.cast(Type.getType(unwrap(source.getRawType())), Type.getType(unwrap(target.getRawType())));
                gen.visitLabel(ifNull);
            }
        },
        OBJECT {
            @Override
            Kind kind(TypeToken<?> source, TypeToken<?> target) {
                if ( target.isAssignableFrom(source) ) {
                    return Kind.SAFE;
                }
                else if ( source.getRawType() == Object.class && !target.isPrimitive() ) {
                    return downcastKind(source, target);
                }
                else if ( (source.getRawType() == Cloneable.class || source.getRawType() == Serializable.class) && target.isArray() ) {
                    return downcastKind(source, target);
                }
                else if ( source.isArray() || target.isArray() ) {
                    int sourceDims = 0;
                    while ( source.isArray() ) {
                        sourceDims++;
                        source = source.getComponentType();
                    }
                    int targetDims = 0;
                    while ( target.isArray() ) {
                        targetDims++;
                        target = target.getComponentType();
                    }
                    if ( sourceDims == targetDims ) {
                        if ( source.isPrimitive() && target.isPrimitive() ) {
                            return source.equals(target) ? Kind.SAFE : null;
                        }
                        else if ( source.isPrimitive() || target.isPrimitive() ) {
                            return null;
                        }
                        return kind(source, target);
                    }
                    else if ( sourceDims > targetDims ) {
                        return target.getRawType() == Object.class ? Kind.SAFE : null;
                    }
                    else {
                        return null;
                    }
                }
                else if ( source.isAssignableFrom(target) ) {
                    return downcastKind(source, target);
                }
                else if ( target.getRawType().isInterface() && !Modifier.isFinal(source.getRawType().getModifiers()) ) {
                    return downcastKind(source, target);
                }
                else {
                    return null;
                }
            }

            private Kind downcastKind(TypeToken<?> source, TypeToken<?> target) {
                if ( target.getRawType().getTypeParameters().length > 0 /*|| source.getRawType().getTypeParameters().length > 0*/ ) {
                    return Kind.UNCHECKED;
                }
                else {
                    return Kind.DOWNCAST;
                }
            }

            @Override
            Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value) {
                return value;
            }

            @Override
            void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) throws IncompatibleTypesException {
                gen.checkCast(Type.getType(target.getRawType()));
            }
        };

        abstract Kind kind(TypeToken<?> source, TypeToken<?> target);
        abstract Object perform(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, Object value);
        abstract void generate(Collection<Kind> kinds, TypeToken<?> source, TypeToken<?> target, GeneratorAdapter gen) throws IncompatibleTypesException;

        private static final List<Class<?>> intPrimitives = Arrays.asList(byte.class, short.class, int.class, long.class);

        static Kind primitiveKind(Class<?> source, Class<?> target) {
            if ( source == boolean.class ) {
                return target == boolean.class ? Kind.SAFE : null;
            }
            else if ( source == double.class ) {
                if ( target == float.class ) {
                    return Kind.UNSAFE;
                }
                else {
                    return Kind.FLOAT_INT_MIX;
                }
            }
            else if ( source == float.class ) {
                if ( target == double.class ) {
                    return Kind.SAFE;
                }
                else {
                    return Kind.FLOAT_INT_MIX;
                }
            }
            else if ( target == double.class || target == float.class ) {
                return Kind.FLOAT_INT_MIX;
            }
            else if ( source == char.class ) {
                return intPrimitives.indexOf(int.class) > intPrimitives.indexOf(target) ? Kind.UNSAFE : Kind.SAFE;
            }
            else {
                return intPrimitives.indexOf(source) > intPrimitives.indexOf(target) ? Kind.UNSAFE : Kind.SAFE;
            }
        }

        boolean containsVoid(TypeToken<?> source, TypeToken<?> target) {
            return source.getRawType() == void.class || target.getRawType() == void.class
                    || source.getRawType() == Void.class || target.getRawType() == Void.class;
        }

    }

    public static interface CastFunction<T, R> extends Function<T, R> {

        Kind getKind();

    }

    private static abstract class AbstractCastFunction<T, R> implements CastFunction<T, R> {
        private final Kind kind;
        protected AbstractCastFunction(Kind kind) {
            this.kind = kind;
        }
        public Kind getKind() {
            return kind;
        }
    }

    private static final class MethodCastFunction<T, R> extends AbstractCastFunction<T, R> {

        private final EnumSet<Kind> kinds;
        private final TypeToken<?> source;
        private final TypeToken<?> target;
        private final CastMethod method;

        private MethodCastFunction(Kind kind, EnumSet<Kind> kinds, TypeToken<?> source, TypeToken<?> target, CastMethod method) {
            super(kind);
            this.kinds = kinds;
            this.source = source;
            this.target = target;
            this.method = method;
        }

        @SuppressWarnings("unchecked")
        @Override
        public R apply(T value) {
            if ( value != null ) {
                TypeToken<?> input = TypeToken.of(value.getClass());
                if ( !source.isAssignableFrom(input) ) {
                    throw new ClassCastException(input + " -> " + source);
                }
            }
            return (R)method.perform(kinds, source, target, value);
        }
    }

    /**
     * Specifies the kind of a cast. Note that {@link #SAFE} is *always* enabled. If you don't want to enable
     * widening casts and (un)boxing, you won't use this class.
     */
    public static enum Kind {
        /**
         * A safe cast like `String` &rarr; `Object` or `int` &rarr; `long`. Note that `WIDENING` actually also
         * includes includes "non-casts" like `String` &rarr; `String`.
         */
        SAFE,
        /**
         * An unsafe narrowing cast involving primitives or their wrappers. `long` &rarr; `int`, `Long` &rarr; `int`
         * or `Double` &rarr `Float` are examples for these.
         */
        UNSAFE,
        /**
         * A cast that mixes floating point numbers and integers, e.g. `long` &rarr; `Double` or `float` &rarr `int`.
         */
        FLOAT_INT_MIX,
        /**
         * A downcast, e.g. `Number` &rarr; `Integer` or `Object` &rarr; `String`.
         */
        DOWNCAST,
        /**
         * A downcast that targeting a generic type like `Object` &rarr; `Function<String>` or
         * `Callable<? extends Number>` &rarr; `Callable<Integer>`.
         */
        UNCHECKED,
        /**
         * A cast involving `void`. If the target is `void`, the value will be dropped. If the source is `void`, the
         * value will be `null`, except if it's a primitive, in which case the target will be the default value for
         * that primitive.
         *
         * @see #DEFAULTS
         */
        VOID,
        /**
         * A special kind for the cast from a wrapper to a primitive. Usually, if the wrapper is `null` the cast will
         * throw a NullPointerException. If `DEFAULTS` is enabled, `null` will be replaced by the `DEFAULT` for the
         * primitive.
         *
         * Note that this flags only affects unboxing. {@link #VOID} will *not* be affected by this flag.
         *
         * @see #VOID
         */
        DEFAULTS
    }

}
