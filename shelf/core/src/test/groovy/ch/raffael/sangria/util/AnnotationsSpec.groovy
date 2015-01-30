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

package ch.raffael.sangria.util

import ch.raffael.sangria.dynamic.Annotations
import spock.lang.Specification


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class AnnotationsSpec extends Specification {

    def "Values are set and retrieved correctly"() {
      when:
        def a = Annotations.fromMap(TwoInts, [one: 11, two: 22])
        println a.getClass()

      then:
        a.one() == 11
        a.two() == 22
    }

    def "Default values are used"() {
      when:
        def a = Annotations.fromMap(TwoInts, [one: 23])
        println a.getClass()

      then:
        a.one() == 23
        a.two() == 42
    }

    def "Missing value throws IllegalArgumentException"() {
      when:
        def a = Annotations.fromMap(TwoInts, [two: 23])

      then:
        thrown IllegalArgumentException
    }

    def "equals() and hashCode() work as specified"() {
      given:
        def rand = new Random(System.currentTimeMillis() * iteration)

      when:
        def a1 = Annotations.fromMap(TwoInts, [one: rand.nextInt(), two: rand.nextInt()])
        def a2 = Annotations.fromMap(TwoInts, [one: a1.one(), two: a1.two()])
        def b = Annotations.fromMap(TwoInts, [one: rand.nextInt(), two: rand.nextInt()])

      then:
        a1 == a2
        a1.hashCode() == a2.hashCode()
        a1 != b

      where:
        iteration << (1..10 as List)
    }

    def "equals() and hashCode() can interact with annotations from reflection"() {
      given:
        def rand = new Random(System.currentTimeMillis() * iteration)
        def val1 = rand.nextInt()
        def val2 = rand.nextInt()

      when:
        def reflect = new GroovyClassLoader(getClass().getClassLoader())
                .parseClass("@ch.raffael.sangria.util.TwoInts(one=$val1,two=$val2) class Foo {}")
                .getAnnotation(TwoInts)
        def eq = Annotations.fromMap(TwoInts, [one: val1, two: val2])
        def ne = Annotations.fromMap(TwoInts, [one: val1 - 1, two: val2 + 2])

      then:
        reflect == eq
        eq == reflect
        reflect.hashCode() == eq.hashCode()
        eq.hashCode() == reflect.hashCode()
        val1 - 1 == val2 + 2 || reflect != ne // random values, so don't let the test fail of 'ne' is actually equal

      where:
        iteration << (1..10 as List)
    }

    def "Throws exception if unknown values specified"() {
      when:
        Annotations.newInstance(TwoInts, 'one', 1, 'foo', 'foo', 'two', 2, 'bar', Void.class, 'one', 11)

      then:
        def e = thrown IllegalArgumentException
        e.message.endsWith(": 'foo', 'bar'")
    }

}
