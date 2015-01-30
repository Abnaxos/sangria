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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.util.concurrent.Uninterruptibles;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class ParallelEventCompletion<T> implements EventCompletion<T> {

    //private final SettableFuture<T> future = SettableFuture.create();
    private final EventBus eventBus;
    private final T event;

    private final Queue<Consumer<? super EventCompletion<? super T>>> completionConsumers = new LinkedList<>();

    private final AtomicReference<List<Throwable>> exceptions = new AtomicReference<>();

    private volatile boolean registrationComplete = false;
    private final AtomicInteger pendingInvocations = new AtomicInteger();
    private final CountDownLatch completionSync = new CountDownLatch(1);

    private volatile boolean dead = true;

    private Stage stage = Stage.DELIVERY;

    public ParallelEventCompletion(EventBus eventBus, T event) {
        this.eventBus = eventBus;
        this.event = event;
    }

    @Override
    public EventCompletion<T> afterCompletion(Consumer<? super EventCompletion<? super T>> consumer) {
        synchronized ( completionConsumers ) {
            if ( stage == Stage.COMPLETE ) {
                invokeLateCompletion(consumer);
            }
            else {
                completionConsumers.offer(consumer);
            }
        }
        return this;
    }

    @Override
    public T getEvent() {
        return event;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public List<Throwable> getExceptions() {
        List<Throwable> currentExceptions = exceptions.get();
        if ( currentExceptions == null ) {
            return ImmutableList.of();
        }
        else {
            return Collections.unmodifiableList(currentExceptions);
        }
    }

    @Override
    public boolean isComplete() {
        return completionSync.getCount() == 0;
    }

    @Override
    public boolean isDead() {
        return isComplete() && dead;
    }

    @Override
    public ParallelEventCompletion<T> await() throws InterruptedException {
        completionSync.await();
        return this;
    }

    @Override
    public ParallelEventCompletion<T> awaitUninterruptibly() {
        Uninterruptibles.awaitUninterruptibly(completionSync);
        return this;
    }

    @Override
    public ParallelEventCompletion<T> await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
        if ( !completionSync.await(time, unit) ) {
            throw new TimeoutException();
        }
        return this;
    }

    @Override
    public ParallelEventCompletion<T> awaitUninterruptibly(long time, TimeUnit unit) throws TimeoutException {
        if ( !Uninterruptibles.awaitUninterruptibly(completionSync, time, unit) ) {
            throw new TimeoutException();
        }
        return this;
    }

    void scheduleInvocation(EventBus.Handler<?> handler) {
        pendingInvocations.incrementAndGet();
        if ( dead ) {
            dead = false;
        }
    }

    void allInvocationsScheduled() {
        registrationComplete = true;
        checkForCompletion();
    }

    void invocationComplete(EventBus.Handler<?> handler, Throwable exception) {
        if ( isComplete() ) {
            throw new IllegalStateException("Event already complete");
        }
        pendingInvocations.decrementAndGet();
        if ( exception != null ) {
            if ( exceptions.get() == null ) {
                exceptions.compareAndSet(null, Collections.synchronizedList(new ArrayList<>()));
            }
            exceptions.get().add(exception);
        }
        checkForCompletion();
    }

    void checkForCompletion() {
        if ( registrationComplete && pendingInvocations.get() <= 0 ) {
            complete();
        }
    }

    private void complete() {
        synchronized ( completionConsumers ) {
            if ( stage != Stage.DELIVERY ) {
                return;
            }
            stage = Stage.COMPLETION;
            completionSync.countDown();
        }
        Consumer<? super EventCompletion<? super T>> consumer;
        while ( (consumer = nextCompletionConsumer()) != null ) {
            invokeCompletion(consumer);
        }
    }

    private Consumer<? super EventCompletion<? super T>> nextCompletionConsumer() {
        synchronized ( completionConsumers ) {
            assert stage == Stage.COMPLETION;
            Consumer<? super EventCompletion<? super T>> consumer = completionConsumers.poll();
            if ( consumer == null ) {
                stage = Stage.COMPLETE;
            }
            return consumer;
        }
    }

    protected void invokeCompletion(Consumer<? super EventCompletion<? super T>> consumer) {
        consumer.accept(this);
    }

    protected void invokeLateCompletion(Consumer<? super EventCompletion<? super T>> consumer) {
        invokeCompletion(consumer);
    }

    @Override
    public String toString() {
        return "ParallelEventCompletion{eventBus=" + eventBus + ",event=" + event + "}";
    }

    public static enum Stage {
        DELIVERY, COMPLETION, COMPLETE;
    }

}
