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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a method as a handler for an application event. The method must take one
 * argument. A second argument of type `ApplicationEventBus` may be specified, to pass
 * a reference to the event bus to the method. This may be used to publish more events or
 * (un)subscribe handlers during event handling.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Subscribe {

    /**
     * Further filter the events. Given, for example, the following event class hierarchy:
     *
     * ![Class Diagram](event-classes.png)
     *
     * And an event handler:
     *
     * ```java
     * {@literal @}Subscribe({FooEvent_A.class, FooEvent_B.class})
     * private void handleFooEvents(FooEvent event) {
     *     // ...
     * }
     * ```
     *
     * This handler would be notified of both `FooEvent_A` and `FooEvent_B` (including `FooEvent_B2`), but not
     * `FooEvent_C`.
     *
     * @startuml event-classes.png
     * hide members
     *
     * FooEvent <|-- FooEvent_A
     * FooEvent <|-- FooEvent_B
     * FooEvent_B <|-- FooEvent_B2
     * FooEvent <|-- FooEvent_C
     * @enduml
     */
    Class<?>[] value() default { };

}
