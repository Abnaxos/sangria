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
public final class Events {

    private Events() {
    }

    public static EventCompletion<DeadEvent> postIfDead(EventCompletion<?> completion) {
        return postIfDead(completion.getEventBus(), completion);
    }

    public static EventCompletion<DeadEvent> postIfDead(EventBus eventBus, EventCompletion<?> completion) {
        if ( completion.isDead() ) {
            return eventBus.post(new DeadEvent(completion));
        }
        else {
            return null;
        }
    }

    public static boolean isAsynchronousEvent(Object event) {
        return isAsynchronousEventType(event.getClass());
    }

    public static boolean isAsynchronousEventType(Class<?> eventType) {
        Event annotation = eventType.getAnnotation(Event.class);
        return annotation != null && annotation.async();
    }
}
