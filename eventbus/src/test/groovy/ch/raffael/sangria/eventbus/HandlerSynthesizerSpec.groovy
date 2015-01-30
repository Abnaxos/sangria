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

import spock.lang.Ignore
import spock.lang.Specification


/**
 * @todo Injections have been removed, re-add them. (Search for "[INJECTIONS]")
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Ignore("Injections have been removed, re-add them. (Search for \"[INJECTIONS]\")")
class HandlerSynthesizerSpec extends Specification {

    EventBus eventBus = Stub(EventBus)

    def "Arguments: Event"() {
      given:
        def args
        def e = new Object()

      when:
        synthesizer.handlers(new Object() {
            @Subscribe
            private void m(Object a) {
                args = [ a ]
            }
        })[0].handler.handleEvent(eventBus, e)

      then:
        args == [ e ]
    }

    def "Arguments: EventBus, Event"() {
      given:
        def args
        def e = new Object()

      when:
        synthesizer.handlers(new Object() {
            @Subscribe
            private void m(EventBus bus, Object a) {
                args = [ bus, a ]
            }
        })[0].handler.handleEvent(eventBus, e)

      then:
        args == [ eventBus, e ]
    }

//[INJECTIONS]
//    def "Arguments: Event, Injector, Integer"() {
//      given:
//        def args
//        def event = new Object()
//
//      when:
//        synthesizer.handlers(new Object() {
//            @Subscribe
//            private void m(Object evt, Injector injector, @Named("myInt") Integer i) {
//                args = [ evt, injector, i]
//            }
//        })[0].handler.handleEvent(eventBus, event)
//
//      then:
//        args == [ event, injector, 42 ]
//    }
//
//    def "Arguments: EventBus, Event, int"() {
//      given:
//        def args
//        def event = new Object()
//
//      when:
//        synthesizer.handlers(new Object() {
//            @Subscribe
//            private void m(EventBus bus, Object evt, @Named("myInt") int i) {
//                args = [ bus, evt, i]
//            }
//        })[0].handler.handleEvent(eventBus, event)
//
//      then:
//        args == [ eventBus, event, 42 ]
//    }

}
