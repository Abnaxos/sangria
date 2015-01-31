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

import java.util.HashMap;
import java.util.Map;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.sangria.commons.annotations.development.Future;
import ch.raffael.sangria.commons.annotations.development.Questionable;

import static java.util.Arrays.asList;


/**
 * Some utilities for working with classes and class loaders.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Classes {

    private static final GetCallerSecurityManager GET_CALLER = new GetCallerSecurityManager();

    private Classes() {
    }

    @NotNull
    public static Class<?> callerClass(@NotNull Class<?> calledClass) {
        return GET_CALLER.getCallerClass(calledClass);
    }

    @NotNull
    @Questionable("While we're using the fastest method possible method to find the caller " +
            "without using sun.*, it's still not very fast -- this does this twice!")
    public static Class<?> callerClass() {
        return GET_CALLER.getCallerClass(callerClass(Classes.class));
    }

    @NotNull
    public static ClassLoader classLoader() {
        return classLoader(null, null);
    }

    @NotNull
    public static ClassLoader classLoader(@Nullable Class fallback) {
        return classLoader(null, fallback);
    }

    @NotNull
    public static ClassLoader classLoader(@Nullable ClassLoader explicit) {
        return classLoader(explicit, null);
    }

    @NotNull
    public static ClassLoader classLoader(@Nullable ClassLoader explicit, @Nullable Class<?> fallback) {
        if ( explicit != null ) {
            return explicit;
        }
        ClassLoader thread = Thread.currentThread().getContextClassLoader();
        if ( thread != null ) {
            return thread;
        }
        if ( fallback == null ) {
            fallback = callerClass(Classes.class);
        }
        return fallback.getClassLoader();
    }

    public static String nameWithoutPackage(Class<?> clazz) {
        int pos = clazz.getName().indexOf('.');
        return clazz.getName().substring(pos + 1);
    }

    public static String canonicalNameWithoutPackage(Class<?> clazz) {
        int pos = clazz.getName().indexOf('.');
        return clazz.getCanonicalName().substring(pos + 1);
    }

    public static Class<?> outermostClass(Class<?> clazz) {
        while ( clazz.getEnclosingClass() != null ) {
            clazz = clazz.getEnclosingClass();
        }
        return clazz;
    }

    public static Class<?> componentType(Class<?> clazz) {
        while ( clazz.isArray() ) {
            clazz = clazz.getComponentType();
        }
        return clazz;
    }

    private final static class GetCallerSecurityManager extends SecurityManager {
        @SuppressWarnings({ "ForLoopReplaceableByForEach" })
        private Class<?> getCallerClass(Class<?> calleeClass) {
            Class<?>[] context = getClassContext();
            int i = 0;
            boolean foundCallee = false;
            for (; i < context.length; i++ ) {
                if ( context[i].equals(calleeClass) ) {
                    foundCallee = true;
                }
                else if ( foundCallee ) {
                    return context[i];
                }
            }
            throw new IllegalStateException("Cannot determine caller of " + calleeClass + " from context " + asList(context));
        }
    }

    @Future
    public static enum NameStyle {
        SOURCE {
            @Override
            public String toSource(String name) {
                return name;
            }
            @Override
            public String toNatural(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toBinary(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toInternal(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toDescriptor(String name) {
                // FIXME: Not implemented
                return null;
            }
        },
        NATURAL {
            @Override
            public String toSource(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toNatural(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toBinary(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toInternal(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toDescriptor(String name) {
                // FIXME: Not implemented
                return null;
            }
        },
        BINARY {
            @Override
            public String toSource(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toNatural(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toBinary(String name) {
                return name;
            }
            @Override
            public String toInternal(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toDescriptor(String name) {
                // FIXME: Not implemented
                return null;
            }
        },
        INTERNAL {
            @Override
            public String toSource(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toNatural(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toBinary(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toInternal(String name) {
                return name;
            }
            @Override
            public String toDescriptor(String name) {
                // FIXME: Not implemented
                return null;
            }
        },
        DESCRIPTOR {
            @Override
            public String toSource(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toNatural(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toBinary(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toInternal(String name) {
                // FIXME: Not implemented
                return null;
            }
            @Override
            public String toDescriptor(String name) {
                return name;
            }
        };

        public abstract String toSource(String name);
        public abstract String toNatural(String name);
        public abstract String toBinary(String name);
        public abstract String toInternal(String name);
        public abstract String toDescriptor(String name);

        private final static Map<String, Class<?>> primitivesByName;
        static {
            Map<String, Class<?>> map = new HashMap<String, Class<?>>();
            map.put("int", int.class);
            map.put("long", long.class);
            map.put("short", short.class);
            map.put("byte", byte.class);
            map.put("char", char.class);
            map.put("double", double.class);
            map.put("float", float.class);
            map.put("boolean", boolean.class);
            map.put("void", void.class);
            primitivesByName = map;
        }
        private final static Map<Class<?>, String> binaryPrimitives;
        static {
            Map<Class<?>, String> map = new HashMap<Class<?>, String>();
            map.put(int.class, "I");
            map.put(long.class, "J");
            map.put(short.class, "S");
            map.put(byte.class, "B");
            map.put(char.class, "C");
            map.put(double.class, "D");
            map.put(float.class, "F");
            map.put(boolean.class, "Z");
            map.put(void.class, "V");
            binaryPrimitives= map;
        }
        private final static Map<String, Class<?>> primitivesByBinary;
        static {
            Map<String, Class<?>> map = new HashMap<String, Class<?>>();
            map.put("I", int.class);
            map.put("J", long.class);
            map.put("S", short.class);
            map.put("B", byte.class);
            map.put("C", char.class);
            map.put("D", double.class);
            map.put("F", float.class);
            map.put("Z", boolean.class);
            map.put("V", void.class);
            primitivesByBinary = map;
        }


    }

}
