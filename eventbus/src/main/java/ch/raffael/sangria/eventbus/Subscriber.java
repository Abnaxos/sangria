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

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Subscriber {

    private final ExecutorService executor;
    private final WeakReference<Object> weakReference;
    private volatile Object hardReference = null;
    private final Queue<Submission> syncedSubmissionQueue = new LinkedList<>();
    private Submission currentSyncedSubmission = null;
    private final Subscription[] subscriptions;

    Subscriber(ExecutorService executor, Object subscriber, Subscription[] subscriptions) {
        this.executor = executor;
        this.weakReference = new WeakReference<>(subscriber);
        this.subscriptions = subscriptions;
    }

    private boolean checkActive() {
        if ( weakReference.get() == null ) {
            syncedSubmissionQueue.clear();
            return false;
        }
        else {
            return true;
        }
    }

    void setWeak(boolean weak) {
        if ( weak ) {
            hardReference = null;
        }
        else {
            hardReference = weakReference.get();
        }
    }

    boolean isWeak() {
        return hardReference != null;
    }

    Object get() {
        return weakReference.get();
    }

    void post(long serial, EventBus eventBus, Object event, ParallelEventCompletion<?> completion) {
        Object subscriber = weakReference.get();
        if ( subscriber == null ) {
            checkActive();
            return;
        }
        boolean sequential = subscriber.getClass().getAnnotation(SequentialEventHandler.class) != null;
        boolean async = Events.isAsynchronousEvent(event) && !sequential;
        Invocation[] invocations = Stream.of(subscriptions)//.parallel()
                .filter(subscription -> subscription.eventType.isInstance(event))
                .map(subscription -> {
                    completion.scheduleInvocation(subscription.handler);
                    return new Invocation(serial, eventBus, event, subscription.handler, completion, async);
                })
                .toArray(Invocation[]::new);
        if ( sequential ) {
            assert !async;
            enqueue(new SequentialSubmission(invocations, completion));
        }
        else if ( async ) {
            new ParallelSubmission(invocations, completion).submit();
        }
        else {
            enqueue(new ParallelSubmission(invocations, completion));
        }
    }

    private void enqueue(Submission submission) {
        synchronized ( syncedSubmissionQueue ) {
            if ( checkActive() ) {
                syncedSubmissionQueue.offer(submission);
                pollQueue();
            }
        }
    }

    @SuppressWarnings("ObjectEquality")
    private void finalizeSubmission(Submission submission) {
        synchronized ( syncedSubmissionQueue ) {
            if ( currentSyncedSubmission == submission ) {
                currentSyncedSubmission = null;
                pollQueue();
            }
        }
    }

    private void pollQueue() {
        if ( checkActive() ) {
            if ( currentSyncedSubmission == null ) {
                currentSyncedSubmission = syncedSubmissionQueue.poll();
                if ( currentSyncedSubmission != null ) {
                    currentSyncedSubmission.submit();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Subscriber{" + Objects.toString(weakReference.get(), "GarbageCollected") + "}";
    }

    static class Subscription {
        private final Class<?> eventType;
        private final EventBus.Handler<?> handler;
        Subscription(Class<?> eventType, EventBus.Handler<?> handler) {
            this.eventType = eventType;
            this.handler = handler;
        }
        @Override
        public String toString() {
            return "Subscription{" + eventType + "->" + handler + "}";
        }
    }

    abstract class Submission {
        abstract void submit();
    }

    class ParallelSubmission extends Submission {

        private final Invocation[] invocations;
        private final ParallelEventCompletion<?> completion;

        ParallelSubmission(Invocation[] invocations, ParallelEventCompletion<?> completion) {
            this.invocations = invocations;
            this.completion = completion;
        }

        @Override
        public void submit() {
            AtomicInteger counter = new AtomicInteger(invocations.length);
            Arrays.stream(invocations).forEach(invocation -> {
                try {
                    executor.submit(() -> {
                        try  {
                            invocation.run();
                        }
                        finally {
                            if ( counter.decrementAndGet() == 0 ) {
                                finalizeSubmission(this);
                            }
                        }
                    });
                }
                catch ( Throwable e ) {
                    try {
                        if ( counter.decrementAndGet() == 0 ) {
                            finalizeSubmission(this);
                        }
                    }
                    finally {
                        completion.invocationComplete(invocation.getHandler(), e);
                    }
                }
            });
        }
    }

    class SequentialSubmission extends Submission implements Runnable {

        private final Invocation[] invocations;
        private final ParallelEventCompletion<?> completion;

        SequentialSubmission(Invocation[] invocations, ParallelEventCompletion<?> completion) {
            this.invocations = invocations;
            this.completion = completion;
        }

        @Override
        public void run() {
            try {
                Arrays.stream(invocations).forEach(Runnable::run);
            }
            finally {
                finalizeSubmission(this);
            }
        }

        @Override
        void submit() {
            try {
                executor.submit(this);
            }
            catch ( Throwable e ) {
                Arrays.stream(invocations).forEach(invocation -> completion.invocationComplete(invocation.getHandler(), e));
            }
        }
    }

}
