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

import ch.raffael.sangria.annotations.Phase
import ch.raffael.sangria.libs.guava.collect.Iterators
import ch.raffael.sangria.testutil.AssemblySpec


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class LoadAssemblyAdviceSpec extends AssemblySpec {

    private final ResourceLocator resourceLocator = new ResourceLocator() {
        @Override
        URL getResource(String name) {
            LoadAssemblyAdviceSpec.this.classLoader.getResource(name)
        }
        @Override
        Iterator<URL> getResources(String name) throws IOException {
            Iterators.forEnumeration(LoadAssemblyAdviceSpec.this.classLoader.getResources(name))
        }
    }

    def setup() {
        nocap('')
        cap('cap')
    }

    def "Load package advices"() {
      when:
        def advice = AssemblyAdvice.forPackage(resourceLocator, cname(''))

      then:
        advice.feature()
        advice.extend() == ['extends.this', 'extends.that' ] as Set
        advice.provides() == ['provide.this'] as Set
        advice.outboundLinks() == ['external.pkg', 'more.(pack, ages)', 'this.rel'] as Set
        advice.linkTo() == ['linked.pkg, super' ] as Set
        advice.usedFeatures() == ['use.this'] as Set
    }

    def "Load class advices, non-accessible work just fine"() {
      when:
        def advice = AssemblyAdvice.forClass(resourceLocator, cname('Advice'))
        def confInstall = AssemblyAdvice.forClass(resourceLocator, cname('ConfInstall'))

      then:
        advice.install() == [ Phase.RUNTIME ] as Set
        advice.dependencies() == [ cname('cap.NotLoadableModule') ] as Set
        advice.usedFeatures() == [ 'use.this' ] as Set
        confInstall.install() == [ Phase.CONFIGURATION ] as Set
    }

}
