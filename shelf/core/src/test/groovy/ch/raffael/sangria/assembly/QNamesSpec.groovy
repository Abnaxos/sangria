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

package ch.raffael.sangria.assembly

import spock.lang.Specification


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class QNamesSpec extends Specification {

    def "A simple QName"() {
      when:
        def result = QNames.parseQNameList('foo.bar', 'ch.raffael.foo.bar')

      then:
        result == ['ch.raffael.foo.bar'] as Set
    }

    def "A simple relative QName"() {
      when:
        def result = QNames.parseQNameList('ch.raffael', 'this.foo.bar')

      then:
        result == ['ch.raffael.foo.bar'] as Set
    }

    def "A simple relative (super) QName"() {
      when:
        def result = QNames.parseQNameList('ch.raffael', 'super.foo.bar')

      then:
        result == ['ch.foo.bar'] as Set
    }

    def "A simple relative (2xsuper) QName"() {
      when:
        def result = QNames.parseQNameList('ch.raffael.deep', 'super.super.foo.bar')

      then:
        result == ['ch.foo.bar'] as Set
    }

    def "A relative QName with inclusive separators"() {
      when:
        def result = QNames.parseQNameList('ch.raffael', 'this+foo+bar.qux+foobar')

      then:
        result == [
                'ch.raffael',
                'ch.raffael.foo',
                'ch.raffael.foo.bar.qux',
                'ch.raffael.foo.bar.qux.foobar'
        ] as Set
    }

    def "A relative (super) QName with inclusive separators"() {
      when:
        def result = QNames.parseQNameList('ch.raffael.go.deeper', 'super+super.super+foo+bar')

      then:
        result == [
                'ch.raffael.go',
                'ch',
                'ch.foo',
                'ch.foo.bar'
        ] as Set
    }

    def "A package name containing only relative packages"() {
      when:
        def result = QNames.parseQNameList('ch.raffael.foo.bar', 'super.super')

      then:
        result == [ 'ch.raffael' ] as Set
    }

    def "A simple group"() {
      when:
        def result = QNames.parseQNameList(null, 'ch.raffael.(foo, bar)')

      then:
        result == [
                'ch.raffael.foo',
                'ch.raffael.bar'
        ] as Set
    }

    def "A simple group, including"() {
      when:
        def result = QNames.parseQNameList(null, 'ch.raffael+(foo, bar)')

      then:
        result == [
                'ch.raffael',
                'ch.raffael.foo',
                'ch.raffael.bar'
        ] as Set
    }

    def "A group with inclusions"() {
      when:
        def result = QNames.parseQNameList(null, 'ch.raffael.(foo+cux, bar.cux)')

      then:
        result == [
                'ch.raffael.foo',
                'ch.raffael.foo.cux',
                'ch.raffael.bar.cux'
        ] as Set
    }

    def "A group with sub-groups"() {
      when:
        def result = QNames.parseQNameList(null, 'ch.raffael.(foo.(bar, cux), bar+(foo, cux+bar))')

      then:
        result == [
                'ch.raffael.foo.bar',
                'ch.raffael.foo.cux',
                'ch.raffael.bar',
                'ch.raffael.bar.foo',
                'ch.raffael.bar.cux',
                'ch.raffael.bar.cux.bar'
        ] as Set
    }

    def "Multiple expressions may be separated by comma"() {
      when:
        def result = QNames.parseQNameList('ch.raffael.relative', 'ch.raffael.(foo, bar), this.foobar, foo+bar')

      then:
        result == [
                'ch.raffael.foo',
                'ch.raffael.bar',
                'ch.raffael.relative.foobar',
                'foo',
                'foo.bar'
        ] as Set
    }

    def "Too many super refs is an error"() {
      when:
        QNames.parseQNameList('ch.raffael.foo', 'super.super.super.super')

      then:
        def e = thrown InvalidQNameListException
        e.message.endsWith ': super beyond package hierarchy'
    }

    def "Default package is an error"() {
      when:
        QNames.parseQNameList('ch.raffael.foo', 'super.super.super')

      then:
        def e = thrown InvalidQNameListException
        e.message.endsWith ': super beyond package hierarchy'
    }

    def "Relative names cause an error when base is null or empty"() {
      when:
        QNames.parseQNameList(base, expression)

      then:
        def e = thrown InvalidQNameListException
        e.message.contains ': Unexpected token \''

      where:
        base | expression

        null | 'this.foo.bar'
        '  ' | 'super.foo.bar'
    }

    def "Syntax error throws exception: #expr"() {
      when:
        QNames.parseQNameList('ch.raffael.syntax.error', expr)

      then:
        def e = thrown InvalidQNameListException
        e.charInLine == pos

      where:
        expr          | pos

        'x..y'        | 3
        'x++y'        | 3
        'x.+y'        | 3
        'x+.y'        | 3

        'x.'          | 2
        'x+'          | 2
        'x..'         | 3
        'y++'         | 3

        'x.(y,)'      | 6
        'x.(y'        | 4

        'x.()'        | 4
        'x+()'        | 4

        'x.(y).(foo)' | 6

        'x.this'      | 6
        'x.super'     | 7
        'x.(this)'    | 7
        'x.(super)'   | 8
    }

}
