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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class ForwardingEventCompletion<T> implements EventCompletion<T> {

    protected abstract EventCompletion<T> delegate();

    @Override
    public EventCompletion<T> afterCompletion(Runnable runnable) {
        delegate().afterCompletion(runnable);
        return this;
    }

    public EventCompletion<T> afterCompletion(Consumer<? super EventCompletion<? super T>> consumer) {
        delegate().afterCompletion(consumer);
        return this;
    }

    @Override
    public T getEvent() {
        return delegate().getEvent();
    }

    @Override
    public EventBus getEventBus() {
        return delegate().getEventBus();
    }

    @Override
    public List<Throwable> getExceptions() {
        return delegate().getExceptions();
    }

    @Override
    public boolean isDead() {
        return delegate().isDead();
    }

    @Override
    public boolean isComplete() {
        return delegate().isComplete();
    }

    @Override
    public EventCompletion<T> await() throws InterruptedException {
        return delegate().await();
    }

    @Override
    public EventCompletion<T> awaitUninterruptibly() {
        return delegate().awaitUninterruptibly();
    }

    @Override
    public EventCompletion<T> await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
        return delegate().await(time, unit);
    }

    @Override
    public EventCompletion<T> awaitUninterruptibly(long time, TimeUnit unit) throws TimeoutException {
        return delegate().awaitUninterruptibly(time, unit);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + delegate() + "}";
    }
}
