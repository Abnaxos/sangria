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

package ch.raffael.sangria.eventbus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Parameter;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.raffael.sangria.libs.guava.primitives.Primitives;
import ch.raffael.sangria.libs.guava.reflect.TypeToken;

import ch.raffael.sangria.commons.UnreachableCodeError;
import ch.raffael.sangria.dynamic.ClassSynthesizer;
import ch.raffael.sangria.dynamic.Reflection;
import ch.raffael.sangria.dynamic.asm.Opcodes;
import ch.raffael.sangria.dynamic.asm.Type;
import ch.raffael.sangria.dynamic.asm.commons.GeneratorAdapter;

import static ch.raffael.sangria.dynamic.Reflection.IterationMode.EXCLUDE_INTERFACES;
import static ch.raffael.sangria.dynamic.asm.Type.getType;
import static ch.raffael.sangria.dynamic.asm.commons.Method.getMethod;


/**
 * @todo Injections have been removed, re-add them. (Search for "[INJECTIONS]"
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class HandlerSynthesizer extends ReflectiveHandlerFactory {

    private static final Logger log = LoggerFactory.getLogger(HandlerSynthesizer.class);
    public static final Supplier[] NO_INJECTIONS = new Supplier[0];

    private final ClassValue<Synthesizer> synthesizers = new ClassValue<Synthesizer>() {
        @Override
        protected Synthesizer computeValue(Class<?> type) {
            return new Synthesizer(type);
        }
    };

    HandlerSynthesizer() {
    }

    HandlerHolder[] handlers(Object subscriber) {
        Synthesizer synthesizer = synthesizers.get(subscriber.getClass());
        HandlerHolder[] handlers = new HandlerHolder[synthesizer.subscriptions.length];
        for ( int i = 0; i < synthesizer.subscriptions.length; i++ ) {
            Synthesizer.MethodSubscription subscription = synthesizer.subscriptions[i];
            handlers[i] = new HandlerHolder(
                    subscription.eventType.getRawType(),
                    synthesizer.factory(subscription, $Factory.class)
                            .newInstance(subscriber, subscription.injections));
        }
        return handlers;
    }

    private class Synthesizer extends ClassSynthesizer {
        private final Class<?> receiverClass;
        private final MethodSubscription[] subscriptions;

        public Synthesizer(Class<?> receiverClass) {
            super(receiverClass.getClassLoader());
            this.receiverClass = receiverClass;
            subscriptions = Reflection.allMethods(receiverClass, EXCLUDE_INTERFACES).stream()
                    .filter(Reflection.Predicates.notOverridden())
                    .filter(m -> m.getAnnotation(Subscribe.class) != null)
                    .map(MethodSubscription::new)
                    .peek(this::linkGenerator)
                    .toArray(MethodSubscription[]::new);

        }

        private class MethodSubscription extends ClassGenerator implements Opcodes {

            private final java.lang.reflect.Method method;
            private final MethodHandle methodHandle;
            private final Type subscriberType;

            private final Supplier<?>[] injections = NO_INJECTIONS;
            private final TypeToken<?> eventType;
            private final boolean injectBus;

            private MethodSubscription(java.lang.reflect.Method method) {
                super(classifiedType(method.getDeclaringClass(), "EventSubscriber"));
                configure()
                        .interfaces(EventBus.Handler.class)
                        .factoryBaseClass($Factory.class)
                        .access(ACC_FINAL);
                this.method = method;
                method.setAccessible(true);
                try {
                    methodHandle = MethodHandles.lookup().unreflect(method);
                    prebind("handleEvent", methodHandle);
                }
                catch ( IllegalAccessException e ) {
                    throw new UnreachableCodeError(method + " should have been set accessible", e);
                }
                subscriberType = getType(method.getDeclaringClass());
                //
                Parameter[] parameters = method.getParameters();
                if ( parameters.length == 0 ) {
                    throw new SubscriptionException(method + ": No event argument");
                }
                injectBus = EventBus.class.equals(parameters[0].getType());
                int injectionOffset;
                if ( injectBus ) {
                    if ( parameters.length < 2 ) {
                        throw new SubscriptionException(method + ": No event argument");
                    }
                    injectionOffset = 2;
                }
                else {
                    injectionOffset = 1;
                }
                eventType = TypeToken.of(parameters[injectBus ? 1 : 0].getParameterizedType());
                ParallelEventBus.checkEventType(method, eventType);
                // [INJECTIONS]
                //injections = new Supplier<?>[parameters.length - injectionOffset];
                //for ( int i = injectionOffset; i < parameters.length; i++ ) {
                //    Parameter parameter = parameters[i];
                //    injections[i - injectionOffset] = injection(parameter);
                //}
            }

            //[INJECTIONS]
            //private Supplier<?> injection(Parameter parameter) {
            //    if ( EventBus.class.isAssignableFrom(parameter.getType()) ) {
            //        return null;
            //    }
            //    return Injections.of(injector, parameter, parameter.getParameterizedType());
            //}

            @Override
            protected void generate() {
                field(ACC_PRIVATE + ACC_FINAL, "subscriber", subscriberType).visitEnd();
                field(ACC_PRIVATE + ACC_FINAL, "injections", getType(Supplier[].class)).visitEnd();
                genConstructor();
                genHandler();
            }

            private void genConstructor() {
                GeneratorAdapter gen = method(ACC_PRIVATE, getMethod("void <init>(java.lang.Object,java.util.function.Supplier[])"));
                gen.visitCode();
                gen.loadThis();
                //S: this
                gen.invokeConstructor(superType(), getMethod("void <init>()"));
                //S:
                gen.loadThis();
                //S: this
                gen.loadArg(0);
                //S: this, subscriber
                gen.checkCast(subscriberType);
                gen.putField(targetType(), "subscriber", subscriberType);
                //S:
                gen.loadThis();
                //S: this
                gen.loadArg(1);
                //S: this, supplier[]
                gen.checkCast(getType(Supplier[].class));
                gen.putField(targetType(), "injections", getType(Supplier[].class));
                //S: this, supplier[]
                gen.returnValue();
                gen.endMethod();
            }

            private void genHandler() {
                GeneratorAdapter gen = method(ACC_PUBLIC, getMethod("void handleEvent(" + EventBus.class.getName() + ",java.lang.Object)"), getType(Exception.class));
                gen.visitCode();
                gen.loadThis();
                //S: this
                gen.getField(targetType(), "subscriber", subscriberType);
                //S: subscriber
                if ( injectBus ) {
                    gen.loadArg(0);
                    //gen.checkCast(getType(EventBus.class));
                }
                //S: subscriber, eventBus?
                gen.loadArg(1);
                //S: subscriber, eventBus?, event
                gen.checkCast(getType(eventType.getRawType()));
                int paramOffset = injectBus ? 2 : 1;
                Parameter[] parameters = method.getParameters();
                for ( int i = paramOffset; i < parameters.length; i++ ) {
                    Parameter injected = parameters[i];
                    gen.loadThis();
                    //S: ... this
                    gen.getField(targetType(), "injections", getType(Supplier[].class));
                    //S: ... injections[]
                    gen.push(i - paramOffset);
                    //S: ... injections[], i
                    gen.arrayLoad(getType(Supplier.class));
                    //S: ... injections[i]
                    gen.invokeInterface(getType(Supplier.class), getMethod("Object get()"));
                    //S: .. injections[i].get()
                    if ( injected.getType().isPrimitive() ) {
                        Type t = getType(Primitives.wrap(injected.getType()));
                        gen.checkCast(t);
                        gen.unbox(getType(injected.getType()));
                    }
                    else {
                        gen.checkCast(getType(injected.getType()));
                    }
                }
                //S: this, eventBus?, event, injections*
                gen.invokeDynamic("handleEvent", methodHandle.type().toMethodDescriptorString(), PreboundClassValues.CONSTANT_BOOTSTRAP);
                gen.returnValue();
                gen.endMethod();
            }

        }
    }

    protected static abstract class $Factory {
        protected abstract EventBus.Handler<?> newInstance(Object subscriber, Supplier[] injections);
    }

}
