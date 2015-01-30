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

package ch.raffael.sangria.eventbus

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@EqualsAndHashCode
@ToString
class Call {

    final Object receiver
    final String method
    final Object event

    Call(Object receiver, String method, Object event) {
        this.receiver = receiver
        this.method = method
        this.event = event
    }

    boolean matches(Call that) {
        return match(receiver, that.receiver) && match(method, that.method) && match(event, that.event)
    }

    private boolean match(Object thisValue, Object thatValue) {
        if ( thisValue == null || thatValue == null ) {
            return true
        }
        else {
            return thisValue == thatValue
        }
    }

    String toString() {
        "$receiver::$method($event)"
    }

}
