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

import java.util.concurrent.atomic.AtomicInteger


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Mixin(MethodName)
class EventReceiver {

    final static serialCounter = new AtomicInteger()

    final serial = serialCounter.getAndIncrement()
    final CallRecorder recorder
    final synchronizers = [:].asSynchronized()

    final invocations = [].asSynchronized()

    def EventReceiver(CallRecorder recorder = new CallRecorder()) {
        this.recorder = recorder
    }

    @Subscribe
    def anEvent(AnEvent evt) {
        recorder.append(this, me(), evt)
        def sync = synchronizers[me()]
        if ( sync != null ) {
            //sleep(100)
            sync.await()
        }
    }

    EventReceiver await(Map<String, Object> syncs) {
        synchronizers.putAll(syncs)
        return this
    }

    @Subscribe
    def fooEvent(FooEvent evt) {
        recorder.append(this, me(), evt)
        synchronizers[me()]?.await()
    }

    @Subscribe
    def barEvent(BarEvent evt) {
        recorder.append(this, me(), evt)
        synchronizers[me()]?.await()
    }

    @Override
    String toString() {
        "${getClass().simpleName}@$serial"
    }
}
