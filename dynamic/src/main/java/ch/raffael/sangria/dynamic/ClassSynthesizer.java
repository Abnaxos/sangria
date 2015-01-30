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

import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.VolatileCallSite;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.raffael.sangria.libs.guava.collect.ImmutableMap;
import ch.raffael.sangria.libs.guava.collect.MapMaker;

import ch.raffael.sangria.dynamic.asm.AnnotationVisitor;
import ch.raffael.sangria.dynamic.asm.ClassReader;
import ch.raffael.sangria.dynamic.asm.ClassVisitor;
import ch.raffael.sangria.dynamic.asm.ClassWriter;
import ch.raffael.sangria.dynamic.asm.FieldVisitor;
import ch.raffael.sangria.dynamic.asm.Handle;
import ch.raffael.sangria.dynamic.asm.Label;
import ch.raffael.sangria.dynamic.asm.Opcodes;
import ch.raffael.sangria.dynamic.asm.Type;
import ch.raffael.sangria.dynamic.asm.commons.GeneratorAdapter;
import ch.raffael.sangria.dynamic.asm.commons.Method;
import ch.raffael.sangria.dynamic.asm.commons.StaticInitMerger;
import ch.raffael.sangria.dynamic.asm.util.ASMifier;
import ch.raffael.sangria.dynamic.asm.util.TraceClassVisitor;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class ClassSynthesizer {

    private static final Pattern SUBSTITUTE_RE = Pattern.compile("\\{([^\\}]*)\\}");
    private static final AtomicLong UID_COUNTER = new AtomicLong();
    private static final boolean DEBUG = Boolean.getBoolean(ClassSynthesizer.class.getName() + ".DEBUG");
    private static final String SYNTH_INTERNAL_PREFIX = "$SYNTH$";

    private final ConcurrentMap<Type, Object> links = new MapMaker().concurrencyLevel(1).makeMap();
    private final ConcurrentMap<String, Supplier<String>> substitutions = new MapMaker().concurrencyLevel(1).makeMap();
    private final ConcurrentMap<ClassGenerator, Class<?>> generatedClasses = new MapMaker().concurrencyLevel(1).makeMap();
    private final Loader loader;

    protected ClassSynthesizer(ClassLoader parentClassLoader) {
        loader = new Loader(parentClassLoader, links, generatedClasses);
        substitution("uid", () -> "$GEN-" + Long.toHexString(UID_COUNTER.getAndIncrement()) + "$");
    }

    private void link(Type type, Object to) {
        Object current = links.putIfAbsent(type, to);
        if ( current != null ) {
            throw new IllegalStateException("Type " + type + " is already bound to " + current);
        }
    }

    protected void linkGenerator(ClassGenerator generator) {
        link(generator.targetType(), generator);
        if ( generator.factoryBaseClass() != null ) {
            FactoryGenerator factoryGenerator = new FactoryGenerator(this, generator);
            generator.factoryGenerator = factoryGenerator;
            link(factoryGenerator.targetType(), factoryGenerator);
        }
    }

    protected void linkClasses(Type type, Class... classes) {
        for ( Class c : classes ) {
            link(type, c);
        }
    }

    protected void linkClasses(Class type, Class... classes) {
        linkClasses(Type.getType(type), classes);
    }

    protected void linkClasses(Type type, Iterable<? extends Class> classes) {
        for ( Class c : classes ) {
            link(type, c);
        }
    }

    protected void linkClasses(Class type, Iterable<? extends Class> classes) {
        linkClasses(Type.getType(type), classes);
    }

    protected void linkMethod(java.lang.reflect.Method method) {
        linkClasses(Type.getType(method.getReturnType()), method.getReturnType());
        for ( Class c : method.getParameterTypes() ) {
            linkClasses(c, c);
        }
        for ( Class c : method.getExceptionTypes() ) {
            linkClasses(c, c);
        }
    }

    protected void linkMethods(java.lang.reflect.Method... methods) {
        for ( java.lang.reflect.Method m : methods ) {
            linkMethod(m);
        }
    }

    protected void linkMethods(Iterable<? extends java.lang.reflect.Method> methods) {
        for ( java.lang.reflect.Method m : methods ) {
            linkMethod(m);
        }
    }

    protected void substitution(String key, Supplier<String> substitutor) {
        Supplier<String> current = substitutions.putIfAbsent(key, substitutor);
        if ( current != null ) {
            throw new IllegalStateException("Substitution for '" + key + "' already bound to " + current);
        }
    }

    protected void substitution(String key, String value) {
        substitution(key, () -> value);
    }

    protected String substitute(String source) {
        Matcher matcher = SUBSTITUTE_RE.matcher(source);
        if ( !matcher.find() ) {
            return source;
        }
        else {
            StringBuffer buf = new StringBuffer();
            do {
                Supplier<String> substitutor = substitutions.get(source);
                if ( substitutor == null ) {
                    throw new IllegalArgumentException("No substitution for '" + matcher.group(1) + "'");
                }
                matcher.appendReplacement(buf, substitutor.get());
            } while ( matcher.find() );
            matcher.appendTail(buf);
            return buf.toString();
        }

    }

    protected String subst(String source) {
        return substitute(source);
    }

    public Class<?> load(ClassGenerator gen) {
        return load(gen, false);
    }

    public Class<?> load(ClassGenerator gen, boolean resolve) {
        Object linked = links.get(gen.targetType());
        if ( !gen.equals(linked) ) {
            throw new IllegalArgumentException("Generator " + gen + " not registered");
        }
        gen = (ClassGenerator)linked;
        if ( gen.generatedClass == null ) {
            try {
                return loader.loadClass(gen.targetType().getClassName(), resolve);
            }
            catch ( ClassNotFoundException e ) {
                throw new ClassSynthesizerException("Unexpectedly unable to load class " + gen.targetType() + "' using generator " + gen, e);
            }
        }
        else {
            return gen.generatedClass;
        }
    }

    public Object factory(ClassGenerator generator) {
        FactoryGenerator factoryGen = generator.factoryGenerator;
        if ( factoryGen == null ) {
            throw new IllegalArgumentException("Class generator " + generator + " has no factory");
        }
        load(factoryGen);
        return factoryGen.instance();
    }

    @SuppressWarnings("unchecked")
    public <T> T factory(ClassGenerator generator, Class<T> expect) {
        Object factory = factory(generator);
        if ( !expect.isInstance(factory) ) {
            throw new ClassCastException("Cannot cast " + factory + " to " + expect);
        }
        return (T)factory;
    }

    /**
     * A base class for all class generators. Implementations will implement {@link #generate()} and
     * possibly override {@link #postProcess(Class)}. It's up to the user where to parametrize the
     * implementation, usually, this will be done in the constructor, however.
     * <p>
     * The default configuration is as follows:
     * <p>
     * *  `superType(Object.class);` *  `interfaces();` *  `access(ACC_PUBLIC | ACC_FINAL |
     * ACC_SUPER);` *  `signature(null);` *  `protectionDomain(null);` *  `javaVersion(V1__8);` *
     * `computeMaxs(true);` *  `computeFrames(true);`
     */
    @SuppressWarnings("UnusedDeclaration")
    public static abstract class ClassGenerator {

        static private final Type T_ARRAYS = Type.getType(Arrays.class);
        static private final Map<Integer, Method> ARRAY_COPY_OF_METHODS = ImmutableMap.<Integer, Method>builder()
                .put(Type.BOOLEAN, Method.getMethod("boolean[] copyOf(boolean[],int)"))
                .put(Type.CHAR, Method.getMethod("char[] copyOf(char[],int)"))
                .put(Type.BYTE, Method.getMethod("byte[] copyOf(byte[],int)"))
                .put(Type.SHORT, Method.getMethod("short[] copyOf(short[],int)"))
                .put(Type.INT, Method.getMethod("int[] copyOf(int[],int)"))
                .put(Type.LONG, Method.getMethod("long[] copyOf(long[],int)"))
                .put(Type.FLOAT, Method.getMethod("float[] copyOf(float[],int)"))
                .put(Type.DOUBLE, Method.getMethod("double[] copyOf(double[],int)"))
                .put(Type.ARRAY, Method.getMethod("Object[] copyOf(Object[],int)"))
                .put(Type.OBJECT, Method.getMethod("Object[] copyOf(Object[],int)"))
                .build();
        protected static final Type T_OBJECT = Type.getType(Object.class);
        protected static final Type T_STRING = Type.getType(String.class);
        protected static final Type T_NULL_POINTER_EXCEPTION = Type.getType(NullPointerException.class);
        protected static final Method M_LOOKUP = Method.getMethod(MethodHandles.Lookup.class.getName() + " lookup()");
        protected static final Type T_METHOD_HANDLES = Type.getType(MethodHandles.class);
        protected static final Type T_LOOKUP = Type.getType(MethodHandles.Lookup.class);
        protected static final String F_PREBOUND = SYNTH_INTERNAL_PREFIX + "PREBOUND";
        protected static final Method M_CLINIT = Method.getMethod("void <clinit>()");
        protected static final Method M_PREBOUND_RETRIEVE = Method.getMethod(PreboundClassValues.class.getName() + " retrieve(" + MethodHandles.Lookup.class.getName() + ")");


        private final PreboundClassValues prebound = new PreboundClassValues();
        private final Type targetType;
        private int flags = ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;
        private int javaVersion = Opcodes.V1_8;
        private Type superType = Type.getType(Object.class);
        private int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER;
        private String signature;
        private Type[] interfaces;
        private ProtectionDomain protectionDomain;
        private Class<?> factoryBaseClass;
        private LinkedList<java.lang.reflect.Method> factoryMethods;

        private FactoryGenerator factoryGenerator;

        private ClassVisitor writer;
        private volatile Class<?> generatedClass;
        private volatile Object factory;

        /**
         * Constructor.
         *
         * @param targetType The ASM Type of the class that will be generated by this generator.
         */
        public ClassGenerator(Type targetType) {
            this.targetType = encodeType(targetType);
        }

        protected Configurator configure() {
            return new Configurator();
        }

        protected static Type classifiedType(Class type, String classifier) {
            return Type.getObjectType(type.getName().replace('.', '/') + "-" + classifier);
        }

        protected static Type classifiedType(Type type, String classifier) {
            return Type.getObjectType(type.getInternalName() + "-" + classifier);
        }

        protected Type encodeType(Type type) {
            String internalName = type.getInternalName();
            int pos = internalName.lastIndexOf('/');
            String packageName;
            String name;
            if ( pos < 0 ) {
                packageName = "";
                name = internalName;
            }
            else {
                packageName = internalName.substring(0, pos);
                name = internalName.substring(pos + 1);
            }
            return Type.getObjectType(packageName + "/$_" + name + UID.next());
        }

        @SuppressWarnings("unchecked")
        private <T, R> R[] convert(Class<R> ret, T[] array, Function<T, R> converter) {
            if ( array == null ) {
                return null;
            }
            else {
                R[] target = (R[])Array.newInstance(ret, array.length);
                for ( int i = 0; i < target.length; i++ ) {
                    target[i] = converter.apply(array[i]);
                }
                return target;
            }
        }

        protected Type targetType() {
            return targetType;
        }

        protected Type superType() {
            return superType;
        }

        protected Type[] interfaces() {
            return interfaces;
        }

        protected Class<?> factoryBaseClass() {
            return factoryBaseClass;
        }

        protected void prebind(String name, Object value) {
            prebound.bindValue(name, value);
        }

        protected ClassVisitor writer() {
            return writer;
        }

        protected GeneratorAdapter method(int access, Method method) {
            return method(access, method, null, (Type[])null);
        }

        protected GeneratorAdapter method(int access, Method method, String signature) {
            return method(access, method, signature, (Type[])null);
        }

        protected GeneratorAdapter method(int access, Method method, Type... exceptions) {
            return method(access, method, null, exceptions);
        }

        protected GeneratorAdapter method(int access, Method method, String signature, Type... exceptions) {
            return new GeneratorAdapter(access, method,
                                        writer.visitMethod(access, method.getName(), method.getDescriptor(), signature,
                                                           convert(String.class, exceptions, Type::getInternalName))
            );
        }

        protected GeneratorAdapter clinit() {
            return method(Opcodes.ACC_STATIC, M_CLINIT);
        }

        protected FieldVisitor field(int access, String name, Type type) {
            return field(access, name, type, null, null);
        }

        protected FieldVisitor field(int access, String name, Type type, String signature) {
            return field(access, name, type, signature, null);
        }

        protected FieldVisitor field(int access, String name, Type type, Object value) {
            return field(access, name, type, null, value);
        }

        protected FieldVisitor field(int access, String name, Type type, String signature, Object value) {
            return writer.visitField(access, name, type.getDescriptor(), signature, value);
        }

        protected void innerClass(Type type, Type outerType, String simpleName, int access) {
            writer.visitInnerClass(type.getInternalName(), outerType == null ? null : outerType.getInternalName(), simpleName, access);
        }

        protected void cloneArray(GeneratorAdapter gen) {
            cloneArray(gen, T_OBJECT);
        }

        protected void cloneArray(GeneratorAdapter gen, Type componentType) {
            Method method = ARRAY_COPY_OF_METHODS.get(componentType.getSort());
            if ( method == null ) {
                throw new IllegalArgumentException("Invalid component type " + componentType);
            }
            //S: array
            gen.dup();
            //S: array, array
            gen.arrayLength();
            //S: array, arrayLen
            gen.invokeStatic(T_ARRAYS, method);
            //S: copyOfArray
        }

        protected void getPrebound(GeneratorAdapter gen, String name, Class expectedType) {
            getPrebound(gen, name, Type.getType(expectedType));
        }

        protected void getPrebound(GeneratorAdapter gen, String name, Type expectedType) {
            gen.getStatic(targetType(), F_PREBOUND, PreboundClassValues.TYPE);
            gen.push(name);
            gen.invokeVirtual(PreboundClassValues.TYPE, Method.getMethod("Object get(String)"));
            gen.checkCast(expectedType);
        }

        protected void requirePrebound(GeneratorAdapter gen, String name, Class expectedType) {
            requirePrebound(gen, name, Type.getType(expectedType));
        }

        protected void requirePrebound(GeneratorAdapter gen, String name, Type expectedType) {
            getPrebound(gen, name, expectedType);
            Label cont = gen.newLabel();
            gen.dup();
            gen.ifNonNull(cont);
            gen.throwException(T_NULL_POINTER_EXCEPTION, "Cannot retrieve prebound value '" + name + "' for " + targetType);
            gen.visitLabel(cont);
        }

        private byte[] doGenerate() {
            try {
                ClassWriter realWriter = new ClassWriter(flags);
                writer = new StaticInitMerger(ClassSynthesizer.class.getName().replace('.', '$') + "-clinit" + "-", realWriter);
                writer.visit(javaVersion, access, targetType.getInternalName(), signature, superType == null ? null : superType.getInternalName(), convert(String.class, interfaces, Type::getInternalName));
                AnnotationVisitor meta = writer.visitAnnotation(Type.getType(MetaData.class).getDescriptor(), true);
                generateMeta(meta);
                meta.visitEnd();
                {
                    field(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, F_PREBOUND, PreboundClassValues.TYPE).visitEnd();
                    GeneratorAdapter gen = clinit();
                    gen.visitCode();
                    gen.invokeStatic(T_METHOD_HANDLES, M_LOOKUP);
                    gen.invokeStatic(PreboundClassValues.TYPE, M_PREBOUND_RETRIEVE);
                    gen.putStatic(targetType(), F_PREBOUND, PreboundClassValues.TYPE);
                    gen.returnValue();
                    gen.endMethod();
                }
                generate();
                writer.visitEnd();
                byte[] bytecode = realWriter.toByteArray();
                if ( DEBUG ) {
                    //noinspection UseOfSystemOutOrSystemErr
                    new ClassReader(bytecode).accept(
                            new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), 0);
                }
                return bytecode;
            }
            finally {
                writer = null;
            }
        }

        void generateMeta(AnnotationVisitor meta) {
            meta.visit("generator", Type.getType(getClass()));
            ZonedDateTime now = ZonedDateTime.now();
            meta.visit("timestamp", now.toInstant().toEpochMilli());
            meta.visit("timestampAsString", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now));
            meta.visit("factory", (this instanceof FactoryGenerator));
        }

        /**
         * Generate the bytecode for the class.
         */
        protected abstract void generate();

        /**
         * Do some post-initialization work on the class. This will be called *before* {@link
         * ClassLoader#findClass(String)} returns. A common use case is binding a {@link
         * ClassSynthesizer.PreboundClassValues} to the generated class.
         * <p>
         * The default implementation does nothing.
         *
         * @param clazz The newly generated class.
         */
        protected void postProcess(Class<?> clazz) {
        }

        protected final class Configurator {
            public Configurator computeMaxs(boolean computeMaxs) {
                if ( computeMaxs ) {
                    flags |= ClassWriter.COMPUTE_MAXS;
                }
                else {
                    flags &= ~ClassWriter.COMPUTE_MAXS;
                }
                return this;
            }

            public Configurator computeFrames(boolean computeFrames) {
                if ( computeFrames ) {
                    flags |= ClassWriter.COMPUTE_FRAMES;
                }
                else {
                    flags &= ~ClassWriter.COMPUTE_FRAMES;
                }
                return this;
            }

            public Configurator javaVersion(int javaVersion) {
                ClassGenerator.this.javaVersion = javaVersion;
                return this;
            }

            public Configurator access(int flags) {
                ClassGenerator.this.access = access | Opcodes.ACC_SUPER;
                return this;
            }

            public Configurator superType(Type type) {
                ClassGenerator.this.superType = type;
                return this;
            }

            public Configurator superType(Class<?> type) {
                return superType(Type.getType(type));
            }

            public Configurator signature(String signature) {
                ClassGenerator.this.signature = signature;
                return this;
            }

            public Configurator interfaces(String... interfaces) {
                ClassGenerator.this.interfaces = convert(Type.class, interfaces, (n) -> Type.getObjectType(n.replace('.', '/')));
                return this;
            }

            public Configurator interfaces(Class... interfaces) {
                ClassGenerator.this.interfaces = convert(Type.class, interfaces, Type::getType);
                return this;
            }

            public Configurator protectionDomain(ProtectionDomain protectionDomain) {
                ClassGenerator.this.protectionDomain = protectionDomain;
                return this;
            }

            public Configurator factoryBaseClass(Class<?> factoryBaseClass) {
                if ( factoryBaseClass != null ) {
                    try {
                        Constructor<?> ctor = factoryBaseClass.getDeclaredConstructor();
                    }
                    catch ( NoSuchMethodException e ) {
                        throw new IllegalArgumentException("Factory base class " + factoryBaseClass + " has no default constructor");
                    }
                    factoryMethods = Reflection.allMethods(factoryBaseClass, Reflection.IterationMode.INTERFACES_EARLY).stream()
                            .filter(Reflection.Predicates.notOverridden())
                            .filter(m -> Modifier.isAbstract(m.getModifiers()))
                            .collect(Collectors.toCollection(LinkedList::new));
                    if ( factoryMethods.isEmpty() ) {
                        factoryMethods = null;
                        throw new IllegalArgumentException("Factory base class " + factoryBaseClass + " has no abstract method");
                    }
                }
                ClassGenerator.this.factoryBaseClass = factoryBaseClass;
                return this;
            }

        }

    }

    /**
     * Some meta data. The synthesizer will annotate synthesized classes with this.
     */
    @SuppressWarnings("UnusedDeclaration")
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface MetaData {
        /**
         * The generator class.
         */
        Class<? extends ClassGenerator> generator();
        /**
         * The timestamp when the class has been generated in milliseconds from 1-1-1970 00:00 UTC.
         */
        long timestamp();
        /**
         * The timestamp when the class has been generated as human readable string.
         */
        String timestampAsString();

        /**
         * `true`, if this is a factory class (bridge to a generated class' constructor(s)).
         */
        boolean factory();
    }

    /**
     * A helper class to transfer {@link java.lang.invoke.MethodHandle}s to the generated class. Method handles can be
     * registered to names, then, when the class is generated, bound to it to retrieve the handles again in the
     * bootstrap methods. It also provides default bootstrap methods that usually just do the Right Thing.
     *
     * **Usage example**
     *
     * ```java
     * // while generating:
     * // GeneratorAdaptor gen;
     * gen.invokeDynamic(myHandleMap.register(myHandle, "fooBar"), myHandle.type().toMethodDescriptorString(), MethodHandleMap.CONSTANT_BOOTSTRAP);
     *
     * // when the class is generated (e.g. in ClassGenerator::postProcess())
     * // Class targetClass;
     * myHandleMap.bind(targetClass)
     * ```
     *
     * @see ClassGenerator#postProcess(Class)
     */
    @SuppressWarnings({ "ObjectEquality", "UnusedDeclaration" })
    public static final class PreboundClassValues {

        public static final Type TYPE = Type.getType(PreboundClassValues.class);

        /**
         * An ASM `Handle` to the default constant call site bootstrapper.
         */
        public static final Handle CONSTANT_BOOTSTRAP = asmHandle("ConstantCallSite");
        /**
         * An ASM `Handle` to the default mutable call site bootstrapper.
         */
        public static final Handle MUTABLE_BOOTSTRAP = asmHandle("MutableCallSite");
        /**
         * An ASM `Handle` to the default volatile call site bootstrapper.
         */
        public static final Handle VOLATILE_BOOTSTRAP = asmHandle("VolatileCallSite");

        private static final ClassValue<AtomicReference<PreboundClassValues>> BINDINGS = new ClassValue<AtomicReference<PreboundClassValues>>() {
            @Override
            protected AtomicReference<PreboundClassValues> computeValue(Class<?> type) {
                return new AtomicReference<>(null);
            }
        };

        private final AtomicBoolean bound = new AtomicBoolean();
        private final ConcurrentMap<String, Object> values = new MapMaker().concurrencyLevel(1).makeMap();

        private PreboundClassValues() {
        }

        /**
         * Register a new handle. An attempt to register another handle under an existing name will result in an
         * {@link IllegalStateException}. It is allowed to register the same handle twice under the same name or several
         * different names:
         *
         * ```java
         * handleMap.register("foo", myHandle); // OK
         * handleMap.register("bar", myHandle); // OK, different name
         * handleMap.register("bar", myHandle); // still OK, same name and same handle
         * handleMap.register("foo", anotherHandle); // IllegalStateException, same name but different handle
         * ```
         *
         * @param name      The name for the handle.
         * @param handle    The method handle.
         *
         * @return The value of the name parameter (shortcut).
         *
         * @throws IllegalStateException If attempting to register a name to a different handle.
         */
        private String bindValue(String name, Object handle) {
            checkNotBound();
            Object current = values.putIfAbsent(name, handle);
            if ( current != null && current != handle ) {
                throw new IllegalStateException("'" + name + "' already bound to handle " + handle);
            }
            return name;
        }

        public Object get(String name) {
            return values.get(name);
        }

        /**
         * Retrieve the handle registered under the given name.
         *
         * @param name    The name of the handle.
         *
         * @return The handle.
         *
         * @throws IllegalStateException If no handle has been registered under the given name.
         */
        public Object require(String name) {
            Object value = values.get(name);
            if ( value == null ) {
                throw new IllegalStateException("No such value: " + name);
            }
            return value;
        }

        /**
         * Bind the handle map to a class. A `MethodHandleMap` can only be bound once and only one `MethodHandleMap` can
         * be bound to a class.
         *
         * @param type    The class to bind the `MethodHandleMap` to.
         *
         * @throws IllegalStateException If either a `MethodHandleMap` has already been bound to the given class or the
         *                               map has already been bound to a different class.
         */
        private void bindTo(Class<?> type) {
            if ( !bound.compareAndSet(false, true) ) {
                throw alreadyBound();
            }
            AtomicReference<PreboundClassValues> boundMap = BINDINGS.get(type);
            if ( boundMap.compareAndSet(null, this) && boundMap.get() != this ) {
                throw new IllegalStateException("Handles for " + type + " already initialized");
            }
        }

        /**
         * Retrieve the MethodHandleMap bound to the specified class throwing an {@link IllegalStateException} if no
         * map has been bound.
         *
         * @param lookup    A {@link MethodHandles.Lookup lookup} of the class requesting the map.
         *
         * @return The bound `MethodHandleMap`.
         *
         * @throws IllegalStateException If no `MethodHandleMap` has been bound to the given class.
         */
        public static PreboundClassValues retrieve(MethodHandles.Lookup lookup) {
            PreboundClassValues map = BINDINGS.get(lookup.lookupClass()).get();
            if ( map == null ) {
                throw new IllegalStateException("No HandleMap registered for " + lookup.lookupClass());
            }
            return map;
        }

        private void checkNotBound() {
            if ( bound.get() ) {
                throw alreadyBound();
            }
        }

        private IllegalStateException alreadyBound() {
            return new IllegalStateException("HandleMap already bound");
        }

        /**
         * Default bootstrapper method for a constant call site. This is usually the one you'll use.
         *
         * @param lookup        The lookup of the calling class.
         * @param name          The name of the method (i.e. the name the handle has been registered with).
         * @param methodType    The method type.
         *
         * @return A {@link java.lang.invoke.ConstantCallSite} f the registered method handle.
         *
         * @throws IllegalStateException If no `MethodHandleMap` has been bound to the given class or no method handle
         *                               has been registered with the given name.
         */
        @SuppressWarnings("UnusedDeclaration")
        public static ConstantCallSite bootstrapConstantCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) {
            return new ConstantCallSite((MethodHandle)retrieve(lookup).require(name));
        }

        /**
         * Default bootstrapper method for a mutable call site. Specifying this method directly as bootstrapper is
         * usually useless because you'll have no access to the {@link java.lang.invoke.MutableCallSite} and therefore no way to mutate
         * it. You might want to use it in your own bootstrapper, though, then store the result somewhere to access
         * it later.
         *
         * @param lookup        The lookup of the calling class.
         * @param name          The name of the method (i.e. the name the handle has been registered with).
         * @param methodType    The method type.
         *
         * @return A {@link ConstantCallSite} for the registered method handle.
         *
         * @throws IllegalStateException If no `MethodHandleMap` has been bound to the given class or no method handle
         *                               has been registered with the given name.
         */
        @SuppressWarnings("UnusedDeclaration")
        public static MutableCallSite bootstrapMutableCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) {
            return new MutableCallSite((MethodHandle)retrieve(lookup).require(name));
        }

        /**
         * Default bootstrapper method for a mutable call site. Specifying this method directly as bootstrapper is
         * usually useless because you'll have no access to the {@link MutableCallSite} and therefore no way to mutate
         * it. You might want to use it in your own bootstrapper, though, then store the result somewhere to access
         * it later.
         *
         * @param lookup        The lookup of the calling class.
         * @param name          The name of the method (i.e. the name the handle has been registered with).
         * @param methodType    The method type.
         *
         * @return A {@link ConstantCallSite} for the registered method handle.
         *
         * @throws IllegalStateException If no `MethodHandleMap` has been bound to the given class or no method handle
         *                               has been registered with the given name.
         */
        @SuppressWarnings("UnusedDeclaration")
        public static VolatileCallSite bootstrapVolatileCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) {
            return new VolatileCallSite((MethodHandle)retrieve(lookup).require(name));
        }

        private static Handle asmHandle(String type) {
            return new Handle(Opcodes.H_INVOKESTATIC, PreboundClassValues.class.getName().replace('.', '/'),
                              "bootstrap" + type,
                              "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/" + type + ";");
        }
    }

    private static final class FactoryGenerator extends ClassGenerator implements Opcodes {

        private final ClassSynthesizer synthesizer;

        private final ClassGenerator target;

        private final AtomicReference<Object> instance = new AtomicReference<>();

        public FactoryGenerator(ClassSynthesizer synthesizer, ClassGenerator target) {
            super(factoryType(target));
            this.synthesizer = synthesizer;
            this.target = target;
            configure()
                    .superType(Type.getType(target.factoryBaseClass))
                    .access(Opcodes.ACC_FINAL);
        }

        @Override
        protected Type encodeType(Type type) {
            return type;
        }

        private static Type factoryType(ClassGenerator target) {
            return Type.getObjectType(
                    target.factoryBaseClass().getName().replace('.', '/') + "-"
                            + target.targetType().getInternalName().replace('/', '!'));
        }

        @Override
        protected void generate() {
            genConstructor();
            target.factoryMethods.forEach(this::genFactoryMethod);
        }

        private void genConstructor() {
            GeneratorAdapter gen = method(Opcodes.ACC_PRIVATE, Method.getMethod("void <init>()"));
            gen.visitCode();
            gen.loadThis();
            gen.invokeConstructor(superType(), Method.getMethod("void <init>()"));
            gen.returnValue();
            //gen.visitInsn(RETURN);
            gen.endMethod();
        }

        private int counter = 0;
        private void genFactoryMethod(java.lang.reflect.Method method) {
            Class<?> targetClass = synthesizer.load(target);
            if ( !method.getReturnType().isAssignableFrom(targetClass) ) {
                throw new IllegalArgumentException("Factory method " + method + " returns incompatible type " + method.getReturnType());
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            String ctorId = "constructor-" + (counter++);
            Constructor<?> ctor;
            MethodHandle ctorHandle;
            try {
                ctor = targetClass.getDeclaredConstructor(parameterTypes);
                ctor.setAccessible(true);
                try {
                    ctorHandle = MethodHandles.lookup().unreflectConstructor(ctor);
                    prebind(ctorId, ctorHandle);
                }
                catch ( IllegalAccessException e ) {
                    throw ReflectionException.propagate(e);
                }
            }
            catch ( NoSuchMethodException e ) {
                throw new IllegalArgumentException("No compatible constructor found for factory method " + method + " in " + targetClass, e);
            }
            GeneratorAdapter gen = method(getAccess(method.getModifiers()), Method.getMethod(method));
            gen.visitCode();
            for ( int i = 0; i < parameterTypes.length; i++ ) {
                gen.loadArg(i);
            }
            gen.invokeDynamic(ctorId, ctorHandle.type().toMethodDescriptorString(), PreboundClassValues.CONSTANT_BOOTSTRAP);
            gen.returnValue();
            gen.endMethod();
        }

        private Object instance() {
            Object ret = instance.get();
            if ( ret == null ) {
                try {
                    Constructor<?> ctor = synthesizer.load(this).getDeclaredConstructor();
                    ctor.setAccessible(true);
                    ret = ctor.newInstance();
                }
                catch ( ReflectiveOperationException e ) {
                    throw ReflectionException.propagate(e);
                }
                if ( !instance.compareAndSet(null, ret) ) {
                    ret = instance.get();
                }
            }
            return ret;
        }

        private int getAccess(int modifiers) {
            if ( Modifier.isPublic(modifiers) ) {
                return Opcodes.ACC_PUBLIC;
            }
            else if ( Modifier.isProtected(modifiers) ) {
                return Opcodes.ACC_PROTECTED;
            }
            else if ( Modifier.isPrivate(modifiers) ) {
                return Opcodes.ACC_PRIVATE;
            }
            else {
                return 0;
            }
        }

    }

    private static final class Loader extends ClassLoader {
        static {
            registerAsParallelCapable();
        }
        private final ConcurrentMap<Type, Object> links;
        private final ConcurrentMap<ClassGenerator, Class<?>> generatedClasses;
        private Loader(ClassLoader parent, ConcurrentMap<Type, Object> links, ConcurrentMap<ClassGenerator, Class<?>> generatedClasses) {
            super(parent);
            this.links = links;
            this.generatedClasses = generatedClasses;
        }
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized ( getClassLoadingLock(name) ) {
                Type type = Type.getObjectType(name.replace('.', '/'));
                Object link = links.get(type);
                if ( link instanceof Class ) {
                    return (Class<?>)link;
                }
                else {
                    return super.loadClass(name, resolve);
                }
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            Object link = links.get(Type.getObjectType(name.replace('.', '/')));
            if ( link instanceof ClassGenerator ) {
                return generateClass((ClassGenerator)link);
            }
            else {
                throw new ClassNotFoundException(name);
            }
        }

        private Class<?> generateClass(ClassGenerator gen) {
            byte[] bytecode = gen.doGenerate();
            Class<?> c = defineClass(gen.targetType().getClassName(), bytecode, 0, bytecode.length, gen.protectionDomain);
            gen.prebound.bindTo(c);
            gen.postProcess(c);
            generatedClasses.put(gen, c);
            return c;
        }

    }

    private final static class UID {
        public static final int COUNTER_MASK = 0xffffff;
        private static final long offset = new Random().nextLong();
        private static final AtomicInteger counter = new AtomicInteger((int)(new Random().nextInt() & COUNTER_MASK));
        static String next() {
            return String.format("-Gen-%x%06x$", (System.nanoTime() + offset), counter.updateAndGet((p) -> (p + 1) & COUNTER_MASK));
        }
    }

}
