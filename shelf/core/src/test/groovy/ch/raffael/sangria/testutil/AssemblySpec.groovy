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

package ch.raffael.sangria.testutil

import ch.raffael.sangria.assembly.Assembly
import ch.raffael.sangria.assembly.AssemblyInfo
import ch.raffael.sangria.assembly.Bundle
import spock.lang.Specification


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
abstract class AssemblySpec extends Specification {

    private final String baseName = findBaseName(getClass())
    protected final CappingClassLoader classLoader = new CappingClassLoader(AssemblySpec.class.classLoader)
    protected final Assembly assembly = new Assembly(AssemblyInfo.builder(getClass().getSimpleName()), classLoader)

    def setup() {
        classLoader.cap(cname(''))
    }

    protected void nocap(String... prefixes) {
        classLoader.nocap(prefixes.collect { prefix -> cname(prefix) })
    }

    protected void cap(String... prefixes) {
        classLoader.cap(prefixes.collect { prefix -> cname(prefix) })
    }

    protected String cname(String name) {
        if ( name.empty ) {
            return baseName
        }
        else if ( name.startsWith('.') ) {
            return name.substring(1)
        }
        else {
            return "$baseName.$name"
        }
    }

    protected String rname(String name) {
        if ( name.startsWith('/') ) {
            return name.substring(1)
        }
        else {
            return "${baseName.replace('.', '/')}/$name"
        }
    }

    protected URL prefixClassPath(String name) {
        return TempFiles.prefixClassPath(cname(name))
    }

    protected Bundle addBundle(String name, Object... classPath) {
        Set urls = (classPath.collect { element ->
            if ( element instanceof String ) {
                return prefixClassPath(element)
            }
            else if ( element instanceof URL ) {
                return (URL)element
            }
            else {
                throw new IllegalArgumentException(classPath.toString())
            }
        }) as Set
        return assembly.addBundle(new URI("test:$name"), name, urls)
    }

    private static String findBaseName(Class clazz) {
        while ( clazz.getEnclosingClass() != null ) {
            clazz = clazz.getEnclosingClass()
        }
        int pos = clazz.getName().lastIndexOf('.')
        if ( pos < 0 ) {
            return '_' + clazz.getName()
        }
        else {
            return clazz.getName().substring(0, pos) + '._' + clazz.getName().substring(pos + 1)
        }
    }

}
