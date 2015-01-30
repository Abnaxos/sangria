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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class TestEventBus implements EventBus {

    private final Object delegateLock = new Object();
    private volatile EventBus delegate = null;
    private final EventBusBuilder builder = EventBus.newParallelEventBus();

    private EventBus delegate() {
        if ( delegate == null ) {
            synchronized ( delegateLock ) {
                if ( delegate == null ) {
                    delegate = builder.build();
                }
            }
        }
        return delegate;
    }

    public Object conf(@DelegatesTo(EventBusBuilder.class) Closure<?> closure) {
        synchronized ( delegateLock ) {
            if ( delegate != null ) {
                throw new IllegalStateException("EventBus already initialized");
            }
            closure = (Closure<?>)closure.clone();
            closure.setDelegate(builder);
            return closure.call();
        }
    }

    @Override
    public <E> EventCompletion<E> post(E event) {
        return delegate().post(event);
    }

    @Override
    public void subscribe(Object object) {
        delegate().subscribe(object);
    }

    @Override
    public void subscribeWeakly(Object object) {
        delegate().subscribeWeakly(object);
    }

    @Override
    public void unsubscribe(Object object) {
        delegate().unsubscribe(object);
    }

    @Override
    public Shutdown shutdown() {
        return delegate().shutdown();
    }

    @Override
    public Shutdown shutdownNow() {
        return delegate().shutdownNow();
    }

    @Override
    public State getState() {
        return delegate().getState();
    }

    public void close() throws InterruptedException {
        shutdownNow().await();
    }

}
