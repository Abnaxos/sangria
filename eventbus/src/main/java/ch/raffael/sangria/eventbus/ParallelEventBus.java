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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import ch.raffael.sangria.libs.guava.collect.MapMaker;
import ch.raffael.sangria.libs.guava.reflect.TypeToken;

import ch.raffael.sangria.dynamic.Reflection;

import static ch.raffael.sangria.dynamic.Reflection.IterationMode.EXCLUDE_INTERFACES;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
//[JMX] @MBean(name = {
//        @MBean.Property(name = "name", generator = ParallelEventBus.JmxName.class),
//        @MBean.Property(name = "serial", generator = ParallelEventBus.JmxSerial.class) })
final class ParallelEventBus implements EventBus, ParallelEventBusMXBean {

    private static final ClassValue<Boolean> MIXED_HANDLER_CACHE = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            return Reflection.allMethods(type, EXCLUDE_INTERFACES).stream()
                    .filter(Reflection.Predicates.notOverridden())
                    .anyMatch(Reflection.Predicates.annotatedWith(Subscribe.class));
        }
    };
    private static final AtomicLong BUS_SERIAL = new AtomicLong();

    private final ReflectiveHandlerFactory reflectiveHandlerFactory;

    private final ConcurrentMap<Object, Subscriber> subscribers;
    private final AtomicLong eventSerial = new AtomicLong();

    private final long serial = BUS_SERIAL.incrementAndGet();
    private final String name;
    private final ExecutorService executor;
    private final IntConsumer subscriberCountConsumer;

    protected ParallelEventBus(ReflectiveHandlerFactory reflectiveHandlerFactory, String name, int subscriptionConcurrencyLevel, ExecutorService executor, IntConsumer subscriberCountConsumer) {
        this.reflectiveHandlerFactory = reflectiveHandlerFactory;
        this.name = name;
        this.executor = executor;
        this.subscriberCountConsumer = subscriberCountConsumer;
        subscribers = new MapMaker()
                .concurrencyLevel(subscriptionConcurrencyLevel).weakKeys().makeMap();
    }

    @Override
    public <E> EventCompletion<E> post(E event) {
        long serial = eventSerial.getAndIncrement();
        ParallelEventCompletion<E> completion = new ParallelEventCompletion<>(this, event);
        subscribers.values().stream().forEach(subscriber -> subscriber.post(serial, this, event, completion));
        completion.allInvocationsScheduled();
        return completion;
    }

    @Override
    public void subscribe(Object object) {
        doSubscribe(object, false);
    }

    @Override
    public void subscribeWeakly(Object object) {
        doSubscribe(object, true);
    }

    @Override
    public void unsubscribe(Object object) {
        subscribers.remove(object);
    }

    @Override
    public Shutdown shutdown() {
        executor.shutdown();
        return createShutdown();
    }

    @Override
    public Shutdown shutdownNow() {
        executor.shutdownNow();
        return createShutdown();
    }

    @Override
    public State getState() {
        if ( executor.isTerminated() ) {
            return State.TERMINATED;
        }
        else if ( executor.isShutdown() ) {
            return State.SHUTDOWN;
        }
        else {
            return State.READY;
        }
    }

    private void doSubscribe(Object object, boolean weak) {
        if ( object instanceof Handler ) {
            if ( MIXED_HANDLER_CACHE.get(object.getClass()) ) {
                throw new IllegalArgumentException("Handler class " + object.getClass().getName() + " mixes handler interface and @Subscribe");
            }
            Subscriber subscriber = subscribers.computeIfAbsent(object, handler -> new Subscriber(executor, handler, new Subscriber.Subscription[] {
                    new Subscriber.Subscription(checkEventType(handler.getClass(), TypeToken.of(handler.getClass()).resolveType(Handler.class.getTypeParameters()[0])).getRawType(), (Handler)handler) }));
            subscriber.setWeak(weak);
        }
        else {
            //return object -> Stream.of(bridges)
            //        .map(bridge -> new Subscription(bridge.getEventType(), bridge.toHandler(object)))
            //        .toArray(Subscription[]::new);
            Subscriber subscriber = subscribers.computeIfAbsent(object, obj -> new Subscriber(
                    executor, object, Stream.of(reflectiveHandlerFactory.handlers(object))
                    .map(holder -> new Subscriber.Subscription(holder.eventType, holder.handler))
                    .toArray(Subscriber.Subscription[]::new)));
            subscriber.setWeak(weak);
        }
        if ( subscriberCountConsumer != null ) {
            subscriberCountConsumer.accept(subscribers.size());
        }
    }

    private Shutdown createShutdown() {
        return new Shutdown() {
            @Override
            public void await() throws InterruptedException {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            }

            @Override
            public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
                return executor.awaitTermination(timeout, unit);
            }
        };
    }

    static TypeToken<?> checkEventType(Object subscriber, TypeToken<?> eventType) {
        if ( eventType.getRawType().isPrimitive() ) {
            throw new SubscriptionException(subscriber + " -> " + eventType.getType() + ": Primitive events not supported");
        }
        else if ( eventType.isArray() ) {
            throw new SubscriptionException(subscriber + " -> " + eventType.getType() + ": Array events not supported");
        }
        else if ( !eventType.isAssignableFrom(eventType.getRawType()) ) {
            throw new SubscriptionException(subscriber + " -> " + eventType.getType() + ": Generic type too specific");
        }
        return eventType;
    }

    @Override
    public String toString() {
        return super.toString() + "{name='" + name + "'}";
    }

    static class JmxName implements Function<ParallelEventBus, String> {
        @Override
        public String apply(ParallelEventBus parallelEventBus) {
            return parallelEventBus.name;
        }
    }

    static class JmxSerial implements Function<ParallelEventBus, String> {
        @Override
        public String apply(ParallelEventBus parallelEventBus) {
            return String.valueOf(parallelEventBus.serial);
        }
    }

}
