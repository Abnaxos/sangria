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

/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
final class Invocation implements Runnable {

    private final long serial;
    private final EventBus eventBus;
    private final Object event;
    private final EventBus.Handler handler;
    private final ParallelEventCompletion<?> completion;
    private final boolean isAsync;

    Invocation(long serial, EventBus eventBus, Object event, EventBus.Handler handler, ParallelEventCompletion<?> completion, boolean isAsync) {
        this.serial = serial;
        this.eventBus = eventBus;
        this.event = event;
        this.handler = handler;
        this.completion = completion;
        this.isAsync = isAsync;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Throwable exception = null;
        try {
            handler.handleEvent(eventBus, event);
        }
        catch ( Throwable e ) {
            exception = e;
        }
        finally {
            completion.invocationComplete(handler, exception);
        }
    }

    long getSerial() {
        return serial;
    }

    Object getEvent() {
        return event;
    }

    ParallelEventCompletion<?> getCompletion() {
        return completion;
    }

    boolean isAsync() {
        return isAsync;
    }

    EventBus getEventBus() {
        return eventBus;
    }

    EventBus.Handler<?> getHandler() {
        return handler;
    }

    @Override
    public String toString() {
        return "Invocation{completion=" + completion + ", handler=" + handler + "}";
    }

}
