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

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class CallRecorder {

    final List<Call> calls = [].asSynchronized()

    CallRecorder append(Object receiver, String method, Object event) {
        append(new Call(receiver, method, event))
    }

    CallRecorder append(Call call) {
        calls << call
        return this
    }

    Matcher matcher(Closure closure) {
        closure = closure.clone()
        def matcher = new Matcher()
        closure.delegate = matcher
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        return matcher
    }

    boolean match(Object receiver, String method, Object event) {
        expect(receiver, method, event).all()
    }

    boolean match(Call call) {
        expect(call).all()
    }

    boolean matchAll(Closure closure) {
        matcher(closure).all()
    }

    boolean matchAll(Call... call) {
        expect(call).all()
    }

    boolean matchAll(Collection<? extends Call> call) {
        expect(call).all()
    }

    Matcher expect(Object receiver, String method, Object event) {
        new Matcher().expect(receiver, method, event)
    }

    Matcher expect(Call... calls) {
        new Matcher().expect(calls)
    }

    Matcher expect(Collection<? extends Call> calls) {
        new Matcher().expect(calls)
    }

    Matcher expect(Call call) {
        new Matcher().expect(call)
    }

    boolean isEmpty() {
        calls.isEmpty()
    }

    String toString() {
        "Recorder$calls"
    }

    class Matcher {

        final List<Call> expect = []
        final List<Call> actual = []

        Matcher expect(Object receiver, String method, Object event) {
            expect(new Call(receiver, method, event))
        }

        Matcher expect(Call... calls) {
            calls.each(this.&expect)
            return this
        }

        Matcher expect(Collection<? extends Call> calls) {
            calls.each(this.&expect)
            return this
        }

        Matcher expect(Call call) {
            if ( calls ) {
                actual << calls.remove(0)
            }
            expect << call
            return this
        }

        boolean all() {
            List<Call> actual = this.actual.clone() as List<Call>
            for ( e in expect ) {
                def found = false
                def iter = actual.iterator()
                while ( iter.hasNext() ) {
                    if ( e.matches(iter.next()) ) {
                        found = true
                        iter.remove()
                        break
                    }
                }
                if ( !found ) {
                    return false
                }
            }
            return actual.empty
        }

        String toString() {
            "actual:$actual\nexpect:$expect"
        }

    }

}
