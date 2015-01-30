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

import ch.raffael.sangria.testutil.AssemblySpec


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class PackageLinkSpec extends AssemblySpec {


    def "Packages can be linked"() {
      when:
        def bundle = addBundle('foo', 'link')

      then:
        bundle.features[cname('link.foo')].packages == [
                cname('link.foo'),
                cname('link.bar'),
                cname('link.bar.foobar'),
                cname('link.abs')
        ] as Set


    }

    def "Packages can be linked to multiple packages, including directly linked packages"() {
      when:
        def bundle = addBundle('foo', 'link')

      then:
        bundle.features[cname('link.multi')].packages == [
                cname('link.multi'),
                cname('link.bar'),
                cname('link.bar.foobar'),
        ] as Set
    }

    def "Packages can be linked to multiple packages, outgoing links will be ignored"() {
      when:
        def bundle = addBundle('foo', 'link')

      then:
        bundle.features[cname('link.multi.partial')].packages == [
                cname('link.bar.foobar'),
                cname('link.multi.partial'),
        ] as Set
    }

    def "Circularities are no problem"() {
      when:
        def bundle = addBundle('foo', 'link')

      then:
        bundle.features[cname('link.circular')].packages == [
                cname('link.circular'),
                cname('link.circular.a'),
                cname('link.circular.b'),
                cname('link.circular.c'),
                cname('link.circular.c.more'),
                cname('link.circular.b.more'),
                cname('link.circular.out'),
                cname('link.circular.self')
        ] as Set
    }

    def "External packages may be linked with @ExternalLink"() {
      when:
        def bundle=addBundle('foo', 'link.external')
      then:
        bundle.features[cname('link.external')].packages == [
                cname('link.external'),
                cname('link.external.ext'),
                cname('link.external.linked'),
                cname('link.external.linked.rel.link'),
                cname('link.external.extext'),
                cname('link.external.extext.linked'),
                cname('link.external.extext.ext'),
                'absolute.pkg'
        ] as Set
    }

}
