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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import ch.raffael.sangria.libs.guava.base.Defaults;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.primitives.Primitives;
import ch.raffael.sangria.libs.guava.reflect.Reflection;

import ch.raffael.sangria.dynamic.asm.Label;
import ch.raffael.sangria.dynamic.asm.Opcodes;
import ch.raffael.sangria.dynamic.asm.Type;
import ch.raffael.sangria.dynamic.asm.commons.GeneratorAdapter;

import static ch.raffael.sangria.dynamic.asm.Type.getType;
import static ch.raffael.sangria.dynamic.asm.commons.Method.getMethod;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Annotations {

    //private static final String ANNOTATION_INSTANCE_PACKAGE = "ch.raffael.sangria.synthetic.annotation";
    private static final String VALUE = "value";

    private final static AtomicInteger uniqueId = new AtomicInteger();

    private static final ClassValue<AnnotationSynthesizer> synthesizers = new ClassValue<AnnotationSynthesizer>() {
        @SuppressWarnings("unchecked")
        @Override
        protected AnnotationSynthesizer computeValue(Class<?> type) {
            if ( !type.isAnnotation() ) {
                throw new IllegalArgumentException(type + " is not an annotation type");
            }
            return new AnnotationSynthesizer((Class<? extends Annotation>)type);
        }
    };


    private Annotations() {
    }

    public static <T extends Annotation> Builder<T> annotation(Class<T> annotationType) {
        return new Builder<>(annotationType);
    }

    public static <T extends Annotation> T fromMap(Class<T> annotationType, Map<String, ?> values) {
        Object[] array = new Object[values.size() * 2];
        int index = 0;
        for ( Map.Entry<String, ?> entry : values.entrySet() ) {
            array[index] = entry.getKey();
            array[index + 1] = entry.getValue();
            index += 2;
        }
        return newInstance(annotationType, array);
    }

    public static <T extends Annotation> T forValue(Class<T> annotationType, Object value) {
        return newInstance(annotationType, VALUE, value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T newInstance(Class<T> annotationType, Object... values) {
        AnnotationSynthesizer synthesizer = synthesizers.get(annotationType);
        return (T)synthesizer.factory(synthesizer.generator, $Factory.class).newInstance(values);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> Class<? extends T> annotationImplementationClass(Class<T> annotationType) {
        AnnotationSynthesizer synth = synthesizers.get(annotationType);
        return (Class<? extends T>)synth.load(synth.generator);
    }

    //public static UniqueId uniqueId() {
    //    return forValue(UniqueId.class, uniqueId.getAndIncrement());
    //}

    public static final class Builder<A extends Annotation> {

        private static final Set<String> EXCLUDED_METHOD_NAMES;
        static {
            ImmutableSet.Builder<String> emb = ImmutableSet.builder();
            Stream.concat(Arrays.stream(Object.class.getDeclaredMethods()), Arrays.stream(Annotation.class.getDeclaredMethods()))
                    .filter((m) -> m.getParameterCount() == 0)
                    .map(Method::getName)
                    .forEach(emb::add);
            EXCLUDED_METHOD_NAMES = emb.build();
        }

        private final Class<A> annotationType;
        private final Map<String, Object> values = new HashMap<>();
        private final A proxy;
        private Method method;

        private Builder(Class<A> annotationType) {
            if ( !annotationType.isAnnotation() ) {
                throw new IllegalArgumentException("Not an annotation type: " + annotationType);
            }
            this.annotationType = annotationType;
            proxy = Reflection.newProxy(annotationType, (p, method, args) -> {
                this.method = method;
                return Defaults.defaultValue(method.getReturnType());
            });
        }

        public <V> Builder<A> set(Function<A, V> annotationMethod, V value) {
            try {
                annotationMethod.apply(proxy);
                if ( method == null ) {
                    throw new IllegalArgumentException("Parameter annotationMethod must be a method reference");
                }
                else if ( !method.getDeclaringClass().equals(annotationType)
                        || method.getParameterCount() > 0
                        || EXCLUDED_METHOD_NAMES.contains(method.getName())
                        || Modifier.isStatic(method.getModifiers()) ) {
                    throw new IllegalArgumentException("Not an annotation method: " + method);
                }
                if ( value == null ) {
                    values.remove(method.getName());
                }
                else {
                    setValue(method, value);
                }
                return this;
            }
            finally {
                method = null;
            }
        }

        public <V> Setter<V> set(Function<A, V> annotationMethod) {
            return new Setter<>(annotationMethod);
        }

        public final class Setter<V> {
            private final Function<A, V> annotationMethod;
            private Setter(Function<A, V> annotationMethod) {
                this.annotationMethod = annotationMethod;
            }
            public Builder<A> to(V value) {
                set(annotationMethod, value);
                return Builder.this;
            }
            public Builder<A> clear() {
                return to(null);
            }
        }

        private void setValue(Method method, Object value) {
            if ( value == null ) {
                values.remove(method.getName());
            }
            else {
                if ( !Primitives.wrap(method.getReturnType()).isInstance(value) ) {
                    throw new IllegalArgumentException("Invalid value for annotation method " + method + ": " + value);
                }
                values.put(method.getName(), value);
            }
        }

        public A get() {
            return Annotations.fromMap(annotationType, values);
        }

    }

    public static abstract class $AbstractAnnotationInstance implements Annotation {

        private final Class<? extends Annotation> annotationType;

        protected $AbstractAnnotationInstance(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public final Class<? extends Annotation> annotationType() {
            return annotationType;
        }

        protected static final class $Support {

            private $Support() {
            }

            public static Object value($AbstractAnnotationInstance self, Object[] values, String name, Class<?> type, Object defaultValue, StringBuilder valuesStringBuilder) {
                Object value = null;
                for ( int i = 0; i < values.length; i += 2 ) {
                    if ( name.equals(values[i]) ) {
                        value = values[i + 1];
                        values[i] = null;
                    }
                }
                if ( value == null ) {
                    if ( defaultValue == null ) {
                        throw new IllegalArgumentException("No value specified for " + self.annotationType.getName() + "::" + name);
                    }
                    value = defaultValue;
                }
                if ( valuesStringBuilder.length() != 0 ) {
                    valuesStringBuilder.append(", ");
                }
                valuesStringBuilder.append(name).append('=');
                if ( value.getClass().isArray() ) {
                    if ( value instanceof int[] ) {
                        valuesStringBuilder.append(Arrays.toString((int[])value));
                    }
                    else if ( value instanceof byte[] ) {
                        valuesStringBuilder.append(Arrays.toString((byte[])value));
                    }
                    else if ( value instanceof short[] ) {
                        valuesStringBuilder.append(Arrays.toString((short[])value));
                    }
                    else if ( value instanceof long[] ) {
                        valuesStringBuilder.append(Arrays.toString((long[])value));
                    }
                    else if ( value instanceof float[] ) {
                        valuesStringBuilder.append(Arrays.toString((float[])value));
                    }
                    else if ( value instanceof double[] ) {
                        valuesStringBuilder.append(Arrays.toString((double[])value));
                    }
                    else if ( value instanceof char[] ) {
                        valuesStringBuilder.append(Arrays.toString((char[])value));
                    }
                    else if ( value instanceof boolean[] ) {
                        valuesStringBuilder.append(Arrays.toString((boolean[])value));
                    }
                    else {
                        valuesStringBuilder.append(Arrays.toString((Object[])value));
                    }
                }
                else {
                    valuesStringBuilder.append(value);
                }
                if ( !Primitives.wrap(type).isInstance(value) ) {
                    throw new IllegalArgumentException("Incompatible type for " + self.annotationType.getName() + "::(" + type + ")" + name + ": (" + value.getClass().getName() + ")" + value);
                }
                return value;
            }

            @SuppressWarnings("UnusedDeclaration")
            public static String init($AbstractAnnotationInstance self, Object[] values, StringBuilder valuesStringBuilder) {
                StringBuilder unknown = null;
                for ( int i = 0; i < values.length; i += 2 ) {
                    if ( values[i] != null ) {
                        if ( unknown == null ) {
                            unknown = new StringBuilder("Unknown values specified for ")
                                    .append(self.annotationType.getName()).append(": ");
                        }
                        else {
                            unknown.append(", ");
                        }
                        unknown.append('\'').append(values[i]).append('\'');
                    }
                }
                if ( unknown != null ) {
                    throw new IllegalArgumentException(unknown.toString());
                }
                return "@" + self.annotationType.getName() + "(" + valuesStringBuilder + ")";
            }

        }

    }

    private static class AnnotationSynthesizer extends ClassSynthesizer {
        private final Class<? extends Annotation> annotationClass;
        private final Type annotationType;
        private final Method[] methods;
        private final Generator generator;
        private AnnotationSynthesizer(Class<? extends Annotation> annotationClass) {
            super(annotationClass.getClassLoader());
            this.annotationClass = annotationClass;
            this.annotationType = getType(annotationClass);
            methods = Arrays.stream(annotationClass.getDeclaredMethods()).filter((m) -> !Modifier.isStatic(m.getModifiers())).toArray(Method[]::new);
            linkGenerator(generator = new Generator());
        }

        private class Generator extends ClassGenerator implements Opcodes {
            private Generator() {
                super(classifiedType(annotationClass, "AnnotationImpl"));
                configure()
                        .superType($AbstractAnnotationInstance.class)
                        .interfaces(annotationClass)
                        .protectionDomain(annotationClass.getProtectionDomain())
                        .factoryBaseClass($Factory.class);
            }

            @Override
            protected void generate() {
                field(ACC_PRIVATE | ACC_FINAL, "stringValue", getType(String.class));
                boolean hasDefaults = false;
                for ( Method method : methods ) {
                    Type retType = getType(method.getReturnType());
                    field(ACC_PRIVATE | ACC_FINAL, "$val$" + method.getName(), retType);
                    if ( method.getDefaultValue() != null ) {
                        hasDefaults = true;
                        field(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "$def$" + method.getName(), getType(Object.class));
                    }
                }
                if ( hasDefaults ) {
                    genClassInit();
                }
                genConstructor();
                for ( Method method : methods ) {
                    genGetter(method);
                }
                genToString();
                genEquals();
                genHashCode();
            }

            private void genClassInit() {
                GeneratorAdapter gen = method(ACC_STATIC, getMethod("void <clinit>()"));
                for ( Method method : methods ) {
                    if ( method.getDefaultValue() != null ) {
                        gen.push(annotationType);
                        //S: targetType
                        gen.push(method.getName());
                        //S: targetType, methodName
                        gen.push(0);
                        //S: targetType, methodName, 0
                        gen.newArray(getType(Class.class));
                        //S: targetType, methodName, Class[0]
                        gen.invokeVirtual(getType(Class.class), getMethod("java.lang.reflect.Method getMethod(String, Class[])"));
                        //S: method
                        gen.invokeVirtual(getType(Method.class), getMethod("Object getDefaultValue()"));
                        //S: defaultValue
                        Label l = gen.newLabel();
                        gen.dup();
                        //S: defaultValue, defaultValue
                        gen.ifNonNull(l);
                        gen.throwException(getType(IllegalStateException.class), "Expected non-null default value for " + method.getName());
                        gen.visitLabel(l);
                        //S: defaultValue
                        gen.putStatic(targetType(), "$def$" + method.getName(), getType(Object.class));
                        //S:
                    }
                }
                gen.returnValue();
                gen.endMethod();
            }

            private void genConstructor() {
                GeneratorAdapter gen = method(ACC_PUBLIC, getMethod("void <init>(Object[])"));
                gen.visitCode();
                int valuesStringBuilder = gen.newLocal(getType(StringBuilder.class));
                gen.loadThis();
                //S: this
                gen.push(annotationType);
                //S: this, annotationType
                gen.invokeConstructor(superType(), getMethod("void <init>(Class)"));
                //S:
                gen.newInstance(getType(StringBuilder.class));
                //S: valuesStringBuilder
                gen.dup();
                //S: valuesStringBuilder, valuesStringBuilder
                gen.invokeConstructor(getType(StringBuilder.class), getMethod("void <init>()"));
                //S: valuesStringBuilder
                gen.storeLocal(valuesStringBuilder);
                //S:
                gen.loadArg(0);
                // S: args
                cloneArray(gen);
                // S: argsCopy
                for ( Method method : methods ) {
                    //S: argsCopy
                    Type retType = getType(method.getReturnType());
                    gen.dup();
                    //S: argsCopy, argsCopy
                    gen.loadThis();
                    //S: argsCopy, argsCopy, this
                    gen.dupX1();
                    //S: argsCopy, this, argsCopy, this
                    gen.swap();
                    //S: argsCopy, this, this, argsCopy
                    gen.push(method.getName());
                    //S: argsCopy, this, this, argsCopy, "fieldName"
                    gen.push(retType);
                    //S: argsCopy, this, this, argsCopy, "fieldName", type
                    if ( method.getDefaultValue() != null ) {
                        gen.getStatic(targetType(), "$def$" + method.getName(), getType(Object.class));
                    }
                    else {
                        gen.visitInsn(ACONST_NULL);
                    }
                    //S: argsCopy, this, this, argsCopy, "fieldName", type, defaultValue
                    gen.loadLocal(valuesStringBuilder);
                    //S: argsCopy, this, this, argsCopy, "fieldName", type, defaultValue, valuesStringBuilder
                    gen.invokeStatic(getType($AbstractAnnotationInstance.$Support.class), getMethod("Object value(" + $AbstractAnnotationInstance.class.getName() + ",Object[],String,Class,Object,StringBuilder)"));
                    //S: argsCopy, this, value
                    gen.unbox(retType);
                    gen.putField(targetType(), "$val$" + method.getName(), retType);
                    //S: argsCopy
                }
                //S: argsCopy
                gen.loadThis();
                //S: argsCopy, this
                gen.swap();
                //S: this, argsCopy
                gen.loadLocal(valuesStringBuilder);
                //S: this, argsCopy, valuesStringBuilder
                gen.invokeStatic(getType($AbstractAnnotationInstance.$Support.class), getMethod("String init(" + $AbstractAnnotationInstance.class.getName() + ",Object[],StringBuilder)"));
                //S: stringValue
                gen.loadThis();
                //S: stringValue, this
                gen.swap();
                //S: this, stringValue
                gen.putField(targetType(), "stringValue", getType(String.class));
                //S:
                gen.visitInsn(RETURN);
                gen.endMethod();
            }

            private void genGetter(Method method) {
                GeneratorAdapter gen = method(ACC_PUBLIC, getMethod(method));
                gen.visitCode();
                gen.loadThis();
                //S: this
                gen.getField(targetType(), "$val$" + method.getName(), getType(method.getReturnType()));
                //S:  value
                if ( method.getReturnType().isArray() ) {
                    cloneArray(gen, getType(method.getReturnType().getComponentType()));
                }
                if ( !method.getReturnType().isPrimitive() ) {
                    gen.checkCast(getType(method.getReturnType()));
                }
                gen.returnValue();
                gen.endMethod();
            }

            private void genToString() {
                GeneratorAdapter gen = method(ACC_PUBLIC, getMethod("String toString()"));
                gen.visitCode();
                gen.loadThis();
                gen.getField(targetType(), "stringValue", T_STRING);
                gen.returnValue();
                gen.endMethod();
            }

            private void genEquals() {
                GeneratorAdapter gen = method(ACC_PUBLIC, getMethod("boolean equals(Object)"));
                final ch.raffael.sangria.dynamic.asm.commons.Method equalsMethod = getMethod("boolean equals(Object)");
                gen.visitCode();
                Label cont;
                // if ( this == that ) return true
                //genPrintln(gen, "Check identity");
                cont = new Label();
                gen.loadThis();
                gen.loadArg(0);
                gen.ifCmp(T_OBJECT, GeneratorAdapter.NE, cont);
                //genPrintln(gen, "Same Instance");
                gen.push(true);
                gen.returnValue();
                gen.visitLabel(cont);
                // if ( !(that instanceof AnnotationType) ) return false;
                //genPrintln(gen, "Check instanceof");
                cont = new Label();
                gen.loadArg(0);
                gen.instanceOf(annotationType);
                gen.ifZCmp(GeneratorAdapter.NE, cont);
                //genPrintln(gen, "Not instanceof");
                gen.push(false);
                gen.returnValue();
                gen.visitLabel(cont);
                // that = (AnnotationType)that
                int that = gen.newLocal(annotationType);
                gen.loadArg(0);
                gen.checkCast(annotationType);
                gen.storeLocal(that, annotationType);
                for ( Method method : methods ) {
                    //genPrintln(gen, "Check value: " + method.getName());
                    Type retType = getType(method.getReturnType());
                    cont = new Label();
                    // if ( !this.value.gen(that.value) ) return false
                    if ( method.getReturnType().isArray() ) {
                        Class<?> arrayType;
                        if ( method.getReturnType().getComponentType().isPrimitive() ) {
                            arrayType = method.getReturnType();
                        }
                        else {
                            arrayType = Object[].class;
                        }
                        gen.loadThis();
                        gen.getField(targetType(), "$val$" + method.getName(), getType(method.getReturnType()));
                        gen.loadLocal(that, annotationType);
                        gen.invokeInterface(annotationType, getMethod(method));
                        gen.invokeStatic(getType(Arrays.class), getMethod("boolean equals(" + arrayType.getComponentType().getName() + "[])"));
                        gen.ifZCmp(GeneratorAdapter.NE, cont);
                    }
                    else if ( method.getReturnType().isPrimitive() ) {
                        gen.loadThis();
                        gen.getField(targetType(), "$val$" + method.getName(), getType(method.getReturnType()));
                        if ( method.getReturnType().equals(double.class) || method.getReturnType().equals(float.class)) {
                            ch.raffael.sangria.dynamic.asm.commons.Method valueOf;
                            if ( method.getReturnType().equals(double.class) ) {
                                valueOf = getMethod("Double valueOf(double)");
                            }
                            else {
                                valueOf = getMethod("Float valueOf(float)");
                            }
                            gen.invokeStatic(getType(Double.class), valueOf);
                            gen.loadLocal(that, annotationType);
                            gen.invokeInterface(annotationType, getMethod(method));
                            gen.invokeStatic(getType(Double.class), valueOf);
                            gen.invokeVirtual(getType(Object.class), getMethod("boolean equals(Object)"));
                        }
                        else {
                            gen.loadLocal(that, annotationType);
                            gen.invokeInterface(annotationType, getMethod(method));
                            gen.ifCmp(retType, GeneratorAdapter.EQ, cont);
                        }
                    }
                    else {
                        gen.loadThis();
                        gen.getField(targetType(), "$val$" + method.getName(), getType(method.getReturnType()));
                        gen.loadLocal(that, targetType());
                        gen.invokeInterface(annotationType, getMethod(method));
                        gen.invokeVirtual(retType, equalsMethod);
                        gen.ifZCmp(GeneratorAdapter.NE, cont);
                    }
                    //genPrintln(gen, "Not equal: " + method.getName());
                    gen.push(false);
                    gen.returnValue();
                    gen.visitLabel(cont);
                }
                // return true
                gen.push(true);
                gen.returnValue();
                gen.endMethod();
            }

            private void genHashCode() {
                GeneratorAdapter gen = method(ACC_PUBLIC, getMethod("int hashCode()"));
                gen.visitCode();
                gen.push(0);
                //S: sum
                for ( Method method : methods ) {
                    gen.push(method.getName().hashCode() * 127);
                    //S: sum, nameHash
                    gen.loadThis();
                    //S: sum, nameHash, this
                    gen.getField(targetType(), "$val$" + method.getName(), getType(method.getReturnType()));
                    //S: sum, nameHash, value
                    if ( method.getReturnType().isPrimitive() ) {
                        gen.invokeStatic(getType(Primitives.wrap(method.getReturnType())), getMethod("int hashCode(" + method.getReturnType().getName() + ")"));
                    }
                    else if ( method.getReturnType().isArray() ) {
                        Class hashCodeArgClass;
                        if ( method.getReturnType().getComponentType().isPrimitive() ) {
                            hashCodeArgClass = method.getReturnType();
                        }
                        else {
                            hashCodeArgClass = Object[].class;
                        }
                        gen.invokeStatic(getType(Arrays.class), getMethod("int hashCode(" + hashCodeArgClass.getComponentType().getName() + "[])"));
                    }
                    else {
                        gen.invokeVirtual(T_OBJECT, getMethod("int hashCode()"));
                    }
                    //S: sum, nameHash, valueHash
                    gen.visitInsn(IXOR);
                    //S: sum, hash
                    gen.visitInsn(IADD);
                    //S: sum
                }
                gen.returnValue();
                gen.endMethod();
            }

        }

    }

    protected static abstract class $Factory {
        protected abstract Object newInstance(Object[] values);
    }

}
