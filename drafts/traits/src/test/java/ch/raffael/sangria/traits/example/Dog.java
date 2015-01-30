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

package ch.raffael.sangria.traits.example;

import ch.raffael.sangria.traits.Traits;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Dog implements Animal, /*@Trait*/ @Word("Wau") Talkative {

    /**
     * The implementation of this constructor will be dynamically generated using `-dynamic`.
     * This functionality will probably also be in a component `-builder`.
     */
    static abstract class Ctor {
        abstract Dog withRace(Object race);
    }

    /**
     * Provide access to the Ctor class.
     */
    public static Ctor newDog() {
        return Traits.constructor(Ctor.class);
    }

    /**
     * Optionally, provide shortcuts.
     */
    public static Dog withRace(Object race) {
        return Traits.constructor(Ctor.class).withRace(race);
    }

    /**
     * General usage.
     */
    public static Dog howToCreateAnInstance() {
        return Dog.newDog().withRace("Bernhardiner");
    }

}
