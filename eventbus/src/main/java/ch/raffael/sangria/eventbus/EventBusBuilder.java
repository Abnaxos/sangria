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

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import ch.raffael.sangria.libs.guava.util.concurrent.MoreExecutors;
import ch.raffael.sangria.libs.guava.util.concurrent.ThreadFactoryBuilder;

import ch.raffael.sangria.commons.Classes;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class EventBusBuilder {

    private static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static Comparator<Runnable> PRIORITY_COMPARATOR = Comparator.comparingInt((o) -> {
        if ( o instanceof Invocation ) {
            Event annotation = ((Invocation)o).getEvent().getClass().getAnnotation(Event.class);
            if ( annotation != null ) {
                return annotation.priority();
            }
            else {
                return 0;
            }
        }
        else {
            return Integer.MIN_VALUE;
        }
    });
    private static Comparator<Runnable> SERIAL_COMPARATOR = Comparator.<Runnable>comparingLong(o -> {
        if ( o instanceof Invocation ) {
            return ((Invocation)o).getSerial();
        }
        else {
            return Long.MAX_VALUE;
        }
    }).reversed();

    private Comparator<Runnable> eventComparator = null;
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    private int maxPoolSize = Integer.MAX_VALUE;
    private Float maxPoolSizeBySubscriber = 1f;
    private long idleTimeout = 60;
    private TimeUnit idleTimeoutUnit = TimeUnit.SECONDS;
    private boolean prestartThreads = false;
    private boolean exiting = false;
    private String name = null;
    private int subscriptionConcurrencyLevel = 1;

    EventBusBuilder() {
    }

    public EventBusBuilder corePoolSize(int corePoolSize) {
        if ( corePoolSize > maxPoolSize ) {
            throw new IllegalArgumentException("corePoolSize(" + corePoolSize + ") > maxPoolSize(" + maxPoolSize + ")");
        }
        if ( corePoolSize < 0 ) {
            throw new IllegalArgumentException("corePoolSize(" + corePoolSize + ") < 0");
        }
        this.corePoolSize = corePoolSize;
        return this;
    }

    public EventBusBuilder corePoolSizeByCore(float corePoolSize) {
        return corePoolSize(Math.round(corePoolSize * CPU_COUNT));
    }

    public EventBusBuilder maxPoolSize(int maxPoolSize) {
        if ( corePoolSize > maxPoolSize ) {
            throw new IllegalArgumentException("corePoolSize(" + corePoolSize + ") > maxPoolSize(" + maxPoolSize + ")");
        }
        if ( maxPoolSize <= 0 ) {
            throw new IllegalArgumentException("maxPoolSize(" + maxPoolSize + ") <= 0");
        }
        maxPoolSizeBySubscriber = null;
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public EventBusBuilder maxPoolSizeByCore(float maxPoolSize) {
        return maxPoolSize(Math.round(maxPoolSize * CPU_COUNT));
    }

    public EventBusBuilder maxPoolSizeBySubscriber(float maxPoolSizeBySubscriber) {
        if ( maxPoolSizeBySubscriber < 0f ) {
            throw new IllegalArgumentException("maxPoolSizeBySubscriber(" + maxPoolSizeBySubscriber + ") < 0");
        }
        this.maxPoolSizeBySubscriber = maxPoolSizeBySubscriber;
        return this;
    }

    public EventBusBuilder fixedPoolSize(int poolSize) {
        maxPoolSize = corePoolSize = poolSize;
        return this;
    }

    public EventBusBuilder fixedPoolSizeByCore(float poolSize) {
        maxPoolSize = corePoolSize = Math.round(poolSize * CPU_COUNT);
        return this;
    }

    public EventBusBuilder serialPrioritized() {
        return addEventComparator(SERIAL_COMPARATOR);
    }

    public EventBusBuilder prioritized() {
        return addEventComparator(PRIORITY_COMPARATOR);
    }

    public EventBusBuilder addEventComparator(Comparator<Runnable> comparator) {
        if ( eventComparator == null ) {
            eventComparator = comparator;
        }
        else {
            eventComparator = eventComparator.thenComparing(comparator);
        }
        return this;
    }

    public EventBusBuilder prestartThreads() {
        prestartThreads = true;
        return this;
    }

    public EventBusBuilder named(String name) {
        this.name = name;
        return this;
    }

    public EventBusBuilder subscriptionConcurrencyLevel(int subscriptionConcurrencyLevel) {
        this.subscriptionConcurrencyLevel = subscriptionConcurrencyLevel;
        return this;
    }

    public EventBus build() {
        return build(new HandlerSynthesizer());
    }

    EventBus build(ReflectiveHandlerFactory reflectiveHandlerFactory) {
        BlockingQueue<Runnable> queue;
        if ( eventComparator == null ) {
            queue = new LinkedBlockingQueue<>();
        }
        else {
            queue = new PriorityBlockingQueue<>(11, eventComparator);
        }
        String name;
        if ( this.name == null ) {
            name = Classes.callerClass(EventBusBuilder.class).getName();
        }
        else {
            name = this.name;
        }
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize, maxPoolSizeBySubscriber != null ? corePoolSize : maxPoolSize,
                idleTimeout, idleTimeoutUnit,
                queue,
                new ThreadFactoryBuilder().setNameFormat("ParallelEventBus[" + name.replaceAll("%", "%%") + "]-%d").build());
        ExecutorService executor;
        if ( exiting ) {
            executor = MoreExecutors.getExitingExecutorService(threadPoolExecutor);
        }
        else {
            executor = Executors.unconfigurableExecutorService(threadPoolExecutor);
        }
        IntConsumer subscriberCountConsumer = null;
        if ( maxPoolSizeBySubscriber != null ) {
            float factor = maxPoolSizeBySubscriber;
            int min = corePoolSize;
            subscriberCountConsumer = count -> threadPoolExecutor.setMaximumPoolSize(Math.max(min, Math.round(factor * count)));
        }
        if ( prestartThreads ) {
            threadPoolExecutor.prestartAllCoreThreads();
        }
        return new ParallelEventBus(reflectiveHandlerFactory, name, subscriptionConcurrencyLevel, executor, subscriberCountConsumer);
    }

}
