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
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ch.raffael.sangria.libs.guava.collect.Iterators;

import ch.raffael.guards.NotNull;
import ch.raffael.sangria.commons.AbstractStreamableIterator;
import ch.raffael.sangria.commons.StreamableIterator;

import static ch.raffael.sangria.dynamic.Reflection.IterationMode.EXCLUDE_INTERFACES;
import static ch.raffael.sangria.libs.guava.collect.Iterators.forArray;
import static ch.raffael.sangria.libs.guava.collect.Iterators.singletonIterator;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Reflection {

    private static final Class[] NO_PARAMETERS = new Class[0];

    private Reflection() {
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Deprecated
    public static boolean equals(Method methodA, Method methodB) {
        if ( methodA == null ) {
            return methodB == null;
        }
        if ( methodA.getParameterCount() != methodB.getParameterCount() ) {
            return false;
        }
        if ( !methodA.getName().equals(methodB.getName()) ) {
            return false;
        }
        return Arrays.equals(methodA.getParameterTypes(), methodB.getParameterTypes());
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Deprecated
    public static boolean equals(Method method, String name, Class... parameterTypes) {
        if ( parameterTypes == null ) {
            parameterTypes = NO_PARAMETERS;
        }
        if ( method == null ) {
            return false;
        }
        if ( method.getParameterCount() != parameterTypes.length ) {
            return false;
        }
        if ( !method.getName().equals(name) ) {
            return false;
        }
        return Arrays.equals(method.getParameterTypes(), parameterTypes);
    }

    public static String getPackageName(Class<?> clazz) {
        return ch.raffael.sangria.libs.guava.reflect.Reflection.getPackageName(clazz);

    }

    public static String getPackageName(String fullClassName) {
        return ch.raffael.sangria.libs.guava.reflect.Reflection.getPackageName(fullClassName);
    }

    public static void initialize(Class... classes) {
        ch.raffael.sangria.libs.guava.reflect.Reflection.initialize(classes);
    }

    public static <T> T newProxy(Class<T> iface, InvocationHandler handler) {
        return ch.raffael.sangria.libs.guava.reflect.Reflection.newProxy(iface, handler);
    }

    public static <T extends Annotation> T getAnnotationFromClassOrPackage(Class<?> type, Class<T> annotationType) {
        T annotation = type.getAnnotation(annotationType);
        if ( annotation == null && type.getPackage() != null ) {
            return type.getPackage().getAnnotation(annotationType);
        }
        else {
            return annotation;
        }
    }

    @NotNull
    public static HierarchyIterator<Class<?>> hierarchy(@NotNull Class<?> start) {
        return hierarchy(start, IterationMode.INTERFACES_EARLY);
    }

    @NotNull
    public static HierarchyIterator<Class<?>> hierarchy(@NotNull Class<?> start, @NotNull IterationMode mode) {
        class InterfaceHierarchyIterator extends AbstractStreamableIterator<Class<?>> {
            private final Set<Class<?>> seen;
            private final LinkedList<Iterator<Class<?>>> stack = new LinkedList<>();
            InterfaceHierarchyIterator(Class<?> clazz) {
                this(clazz, new HashSet<>());
            }
            InterfaceHierarchyIterator(Class<?> clazz, Set<Class<?>> seen) {
                this.seen = seen;
                if ( clazz.isInterface() ) {
                    stack.push(singletonIterator(clazz));
                }
                else {
                    stack.push(forArray(clazz.getInterfaces()));
                }
            }
            @Override
            protected Class<?> computeNext() {
                while ( !stack.isEmpty() ) {
                    while ( stack.peek().hasNext() ) {
                        Class<?> next = stack.peek().next();
                        if ( seen.add(next) ) {
                            Class<?>[] sub = next.getInterfaces();
                            if ( sub.length > 0 ) {
                                stack.push(forArray(sub));
                            }
                            return next;
                        }
                    }
                    stack.pop();
                }
                return endOfData();
            }
            @Override
            public Stream<Class<?>> stream() {
                return builder().ordered().distinct().nonnull().immutable().stream();
            }
        }

        class InterfaceHierarchy implements Iterable<Class<?>> {
            private final Set<Class<?>> seen = new HashSet<>(32);
            private final LinkedList<Class<?>> queue = new LinkedList<>();
            InterfaceHierarchy() {
            }
            void add(Class<?> clazz) {
                if ( clazz.isInterface() ) {
                    queue.add(clazz);
                }
                add(clazz.getInterfaces());
            }
            void add(Class<?>[] classes) {
                for ( Class<?> c : classes ) {
                    assert c.isInterface() : c + " is not an interface";
                    queue.add(c);
                    add(c.getInterfaces());
                }
            }
            boolean isEmpty() {
                return queue.isEmpty();
            }
            @Override
            public Iterator<Class<?>> iterator() {
                return new AbstractStreamableIterator<Class<?>>() {
                    @Override
                    protected Class<?> computeNext() {
                        while ( !queue.isEmpty() ) {
                            Class<?> next = queue.poll();
                            if ( !seen.contains(next) && !Iterators.any(queue.iterator(), c -> !c.equals(next) && next.isAssignableFrom(c)) ) {
                                seen.add(next);
                                return next;
                            }
                        }
                        return endOfData();
                    }
                    @Override
                    public Stream<Class<?>> stream() {
                        return builder().ordered().distinct().nonnull().immutable().stream();
                    }
                };
            }

        }

        class ClassHierarchyIterator extends AbstractStreamableIterator<Class<?>> {
            private final IterationMode iterationMode;
            private final InterfaceHierarchy interfaces;
            private Class<?> currentClass;
            private Iterator<Class<?>> currentInterfaceIterator = null;
            ClassHierarchyIterator(Class<?> start, IterationMode mode) {
                currentClass = start;
                this.iterationMode = mode;
                interfaces = (mode == EXCLUDE_INTERFACES ? null : new InterfaceHierarchy());
            }
            @Override
            protected Class<?> computeNext() {
                if ( currentInterfaceIterator != null ) {
                    if ( !currentInterfaceIterator.hasNext() ) {
                        currentInterfaceIterator = null;
                    }
                    else {
                        return currentInterfaceIterator.next();
                    }
                }
                if ( currentClass == null ) {
                    if ( interfaces != null && !interfaces.isEmpty() ) {
                        currentInterfaceIterator = interfaces.iterator();
                        return computeNext();
                    }
                    return endOfData();
                }
                else {
                    Class<?> next = currentClass;
                    currentClass = currentClass.getSuperclass();
                    if ( interfaces != null ) {
                        interfaces.add(next);
                    }
                    if ( iterationMode == IterationMode.INTERFACES_EARLY ) {
                        interfaces.add(next);
                        currentInterfaceIterator = interfaces.iterator();
                    }
                    else if ( iterationMode == IterationMode.INTERFACES_LATE ) {
                        interfaces.add(next);
                    }
                    return next;
                }
            }
            @Override
            public Stream<Class<?>> stream() {
                return builder().ordered().distinct().nonnull().immutable().stream();
            }
        }

        if ( start.isInterface() ) {
            if ( mode == IterationMode.EXCLUDE_INTERFACES ) {
                return emptyHierarchyIterator() ;
            }
            else {
                return new HierarchyIteratorWrapper<>(new InterfaceHierarchyIterator(start));
            }
        }
        else {
            return new HierarchyIteratorWrapper<>(new ClassHierarchyIterator(start, mode));
        }
    }

    public static StreamableIterator<Method> allMethods(Class<?> start, IterationMode mode) {
        return new AbstractStreamableIterator<Method>() {
            private final HierarchyIterator<Class<?>> classes = hierarchy(start, mode);
            private Iterator<Method> currentMethodsIter = Collections.emptyIterator();
            @Override
            protected Method computeNext() {
                while ( currentMethodsIter.hasNext() || classes.hasNext() ) {
                    if ( currentMethodsIter.hasNext() ) {
                        return currentMethodsIter.next();
                    }
                    else {
                        currentMethodsIter = Iterators.forArray(classes.next().getDeclaredMethods());
                    }
                }
                return endOfData();
            }
            @Override
            public Stream<Method> stream() {
                return builder().ordered().distinct().nonnull().immutable().stream();
            }
        };
    }

    public static StreamableIterator<Method> overriddenMethods(Method start, IterationMode mode) {
        return new AbstractStreamableIterator<Method>() {
            private final Iterator<Method> allMethods = allMethods(start.getDeclaringClass(), mode);
            private Method currentOverrider = start;
            @Override
            protected Method computeNext() {
                while ( allMethods.hasNext() ) {
                    Method m = allMethods.next();
                    if ( m.getDeclaringClass().isInterface() ) {
                        if ( overrides(start, m) ) {
                            return m;
                        }
                    }
                    else {
                        if ( overrides(currentOverrider, m) ) {
                            currentOverrider = m;
                            return m;
                        }
                    }
                }
                return endOfData();
            }
            @Override
            public Stream<Method> stream() {
                return builder().ordered().distinct().nonnull().immutable().stream();
            }
        };
    }

    public static boolean overrides(Method lower, Method upper) {
        return  isCandidateForOverride(lower, upper)
                && parametersMatch(lower, upper);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean isCandidateForOverride(Method lower, Method upper) {
        // parameter count
        if ( lower.getParameterCount() != upper.getParameterCount() ) {
            return false;
        }
        // name
        if ( !lower.getName().equals(upper.getName()) ) {
            return false;
        }
        // class hierarchy
        if ( !upper.getDeclaringClass().isAssignableFrom(lower.getDeclaringClass()) ) {
            return false;
        }
        // visibility
        Visibility upperVisibility = Visibility.of(upper);
        Visibility lowerVisibility = Visibility.of(lower);
        if ( upperVisibility == Visibility.PRIVATE || lowerVisibility == Visibility.PRIVATE ) {
            return false;
        }
        else if ( upperVisibility == Visibility.PACKAGE ) {
            return Objects.equals(upper.getDeclaringClass().getPackage(), lower.getDeclaringClass().getPackage());
        }
        return true;
    }

    /**
     * Returns `true`, if a generic parameter type may change a method signature after erasure.
     * Signature relevant after erasure means, the type has a type variable at it's 'top level',
     * e.g. `T`, `T[]`, `T[][]`, but no parametrized types (`List<T>`) or arrays thereof (`List<T>[]`).
     * @param t
     * @return
     */
    private static boolean isSignatureRelevantAfterErasure(Type t) {
        while ( t instanceof GenericArrayType ) {
            t = ((GenericArrayType)t).getGenericComponentType();
        }
        return t instanceof TypeVariable;
    }

    private static boolean parametersMatch(Method lower, Method upper) {
        Parameter[] lowerParameters = lower.getParameters();
        Parameter[] upperParameters = upper.getParameters();
        if ( lowerParameters.length != upperParameters.length ) {
            return false;
        }
        boolean bridgeNeeded = false;
        for ( int i = 0; i < lowerParameters.length; i++ ) {
            Parameter u = upperParameters[i];
            Parameter l = lowerParameters[i];
            if ( !isSignatureRelevantAfterErasure(u.getParameterizedType()) ) {
                if ( !l.getType().equals(u.getType()) ) {
                    return false;
                }
            }
            else if ( !isSignatureRelevantAfterErasure(l.getParameterizedType()) ) {
                return false;
            }
            else {
                // both are signature relevant, l overrides u if l instanceof u
                if ( !u.getType().isAssignableFrom(l.getType()) ) {
                    return false;
                }
                if ( !bridgeNeeded && !u.getType().equals(l.getType()) ) {
                    bridgeNeeded = true;
                }
            }
        }
        // TODO: check for a bridge? To *really* override, there should be one, somewhere ...
        // However, if both classes were present in its current form, it would have been a compiler
        // error.
        return true;

        // code from commons lang
        //final Map<TypeVariable<?>, Type> typeArguments = Types.getTypeArguments(lower.getDeclaringClass(), upper.getDeclaringClass());
        //for ( int i = 0; i < lowerParameters.length; i++ ) {
        //    final Type childType = Types.unrollVariables(typeArguments, lowerParameters[i]);
        //    final Type parentType = Types.unrollVariables(typeArguments, upperParameters[i]);
        //    if (!Types.equals(childType, parentType)) {
        //        return false;
        //    }
        //}
        //return true;
    }

    public static enum IterationMode {
        INTERFACES_EARLY, INTERFACES_LATE, EXCLUDE_INTERFACES /*FIXME:, EXCLUDE_CLASSES*/;

        public boolean includesInterfaces() {
            return this != EXCLUDE_INTERFACES;
        }

        public boolean includesClasses() {
            return true; // FIXME: this != EXCLUDE_CLASSES
        }
    }

    public static interface HierarchyIterator<T> extends StreamableIterator<T> {
        HierarchyIterator<T> skipRoot();

        @Override
        default Stream<T> stream() {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(
                            this, Spliterator.ORDERED + Spliterator.DISTINCT +
                                    Spliterator.NONNULL + Spliterator.IMMUTABLE),
                    false);
        }
    }

    public static HierarchyIterator EMPTY_HIERARCHY_ITERATOR = new HierarchyIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public HierarchyIterator skipRoot() {
            return this;
        }
    };
    @SuppressWarnings("unchecked")
    private static <T> HierarchyIterator<T> emptyHierarchyIterator() {
        return EMPTY_HIERARCHY_ITERATOR;
    }

    private static class HierarchyIteratorWrapper<T> implements HierarchyIterator<T> {
        private final Iterator<T> delegate;
        private boolean pastRoot = false;
        private HierarchyIteratorWrapper(Iterator<T> delegate) {
            this.delegate = delegate;
        }
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }
        @Override
        public T next() {
            pastRoot = true;
            return delegate.next();
        }
        @Override
        public void remove() {
            delegate.remove();
        }
        public void forEachRemaining(Consumer<? super T> action) {
            delegate.forEachRemaining(action);
        }
        @Override
        public HierarchyIterator<T> skipRoot() {
            if ( !pastRoot ) {
                pastRoot = true;
                if ( hasNext() ) {
                    next();
                }
            }
            return this;
        }
    }

    public static final class Predicates {
        private Predicates() {

        }

        public static Predicate<Method> notOverridden() {
            return new Predicate<Method>() {
                private final Set<Method> overridden = new HashSet<>();
                @Override
                public boolean test(Method method) {
                    if ( overridden.contains(method) ) {
                        return false;
                    }
                    Iterators.addAll(overridden, Reflection.overriddenMethods(method, EXCLUDE_INTERFACES));
                    return true;
                }
            };
        }

        public static <T extends AnnotatedElement> Predicate<T> annotatedWith(Class<? extends Annotation> annotation) {
            Repeatable repeatable = annotation.getAnnotation(Repeatable.class);
            if ( repeatable != null ) {
                return e -> (e.getAnnotationsByType(annotation) != null && e.getAnnotation(repeatable.value()) != null);
            }
            else {
                return e -> (e.getAnnotationsByType(annotation) != null);
            }
        }

    }

}
