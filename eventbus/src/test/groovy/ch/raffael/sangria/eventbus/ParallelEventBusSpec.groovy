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

import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.AutoCleanup
import spock.lang.FailsWith
import spock.lang.Specification

import java.util.concurrent.CyclicBarrier

import static java.util.concurrent.TimeUnit.SECONDS


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class ParallelEventBusSpec extends Specification {

    @AutoCleanup
    def bus = new TestEventBus()
    def recorder = new CallRecorder()

    def "Method subscriptions receive events"() {
      given:

        def rcvA = new EventReceiver(recorder)
        def rcvB = new EventReceiver(recorder)
        def events = [ new AnEvent(), new FooEvent(), new BarEvent() ]
        bus.subscribe(rcvA)
        bus.subscribe(rcvB)

      when:
        events.collect({ bus.post(it) })*.await()

      then:
        recorder.matcher {
            expect(rcvA, 'anEvent', events[0])
            expect(rcvB, 'anEvent', events[0])
        }.all()
        recorder.matcher {
            expect(rcvA, 'anEvent', events[1])
            expect(rcvA, 'fooEvent', events[1])
            expect(rcvB, 'anEvent', events[1])
            expect(rcvB, 'fooEvent', events[1])
        }.all()
        recorder.matcher {
            expect(rcvA, 'anEvent', events[2])
            expect(rcvA, 'barEvent', events[2])
            expect(rcvB, 'anEvent', events[2])
            expect(rcvB, 'barEvent', events[2])
        }.all()
        recorder.empty
    }

    //@Unroll
    def "Events are sent in parallel to different subscribers, but queued for the same subscriber"() {
      given:
        bus.conf {
            maxPoolSize 10
        }
        def recorder = new CallRecorder()
        def events = [ new AnEvent(), new AnEvent() ]
        def barrierA = new CyclicBarrier(2)
        def barrierB = new CyclicBarrier(2)
        def rcvA = new EventReceiver(recorder).await(anEvent: barrierA)
        def rcvB = new EventReceiver(recorder).await(anEvent: barrierB)
        def cmpl = []
        bus.subscribe(rcvA)
        bus.subscribe(rcvB)

      when:
        cmpl << bus.post(events[0])
        barrierA.await(2, SECONDS)
        cmpl << bus.post(events[1])
        barrierA.await(2, SECONDS)

        2.times { barrierB.await(2, SECONDS) }
        cmpl*.await(2, SECONDS)

      then:
        recorder.matcher {
            expect(rcvA, null, events[0])
            expect(rcvB, null, events[0])
            expect(rcvA, null, events[1])
        }.all()
        //recorder.expect(rcvA, null, events[1]).all()
        recorder.expect(rcvB, null, events[1]).all()

      where:
        i << (1..100)
    }

    @FailsWith(
            value = ConditionNotSatisfiedError,
            reason = "The EventBus currently just shuts down the ExecutorService, causing queued events being rejected"
    )
    def "shutdown() sends pending events"() {
      given:
        def events = [ new AnEvent(), new AnEvent() ]
        def barrier = new CyclicBarrier(2)
        def rcv = new EventReceiver(recorder).await(anEvent: barrier)
        bus.subscribe(rcv)

      when:
        bus.post(events[0])
        bus.post(events[1])
        def shutdown = bus.shutdown()
        barrier.await()
        shutdown.await()

      then:
        recorder.matcher {
            expect(null, null, events[0])
            expect(null, null, events[1])
        }.all()
    }

}
