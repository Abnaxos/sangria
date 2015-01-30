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

package ch.raffael.sangria.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.raffael.sangria.libs.guava.base.Joiner;


/**
 * A simple untyped tuple. Mostly useful for composite keys in a map -- you're free to use it for
 * whatever you find it suitable for, though. ;)
 *
 * It's strongly encouraged to extend this class (not anonymously!) to avoid 'false' equals() result
 * and a more informative `toString()`.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Tuple {

    private static final Joiner JOINER = Joiner.on(',');
    protected final Object[] values;

    public Tuple(Object... values) {
        this(false, values);
    }

    public Tuple(Collection<?> collection) {
        values = collection.toArray();
    }

    public Tuple(Iterable<?> values) {
        if ( values instanceof Collection ) {
            this.values = ((Collection)values).toArray();
        }
        else {
            this.values = toArray(values.iterator());
        }
    }

    public Tuple(Iterator<?> values) {
        this(true, toArray(values));
    }

    public Tuple(boolean doNotCopyValues, Object... values) {
        if ( doNotCopyValues ) {
            this.values = values;
        }
        else {
            this.values = values.clone();
        }
    }

    private static Object[] toArray(Iterator<?> iterator) {
        ArrayList<Object> list = new ArrayList<>();
        while ( iterator.hasNext() ) {
            list.add(iterator.next());
        }
        return list.toArray();
    }

    public Object get(int index) {
        return values[index];
    }

    public int size() {
        return values.length;
    }

    public List toList() {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        Tuple that = (Tuple)o;
        return Arrays.deepEquals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(values);
    }

    /**
     * Returns `"ClassName{element,element,...}"`.
     * @return The class name.
     */
    @Override
    public String toString() {
        return JOINER.appendTo(className(new StringBuilder()).append('{'), values).append('}').toString();
    }

    /**
     * Customize the class name representation for {@link #toString()}. The default implementation
     * uses {@link Class#getCanonicalName()} with the package name omitted (e.g. `"MyToken"` or
     * `"Outer.Key"` (for inner classes)).
     *
     * Customize this by overriding this method.
     *
     * @param buf    The StringBuilder to append to.
     *
     * @return The StringBuilder to continue working with (usually the one passed in).
     */
    protected StringBuilder className(StringBuilder buf) {
        return buf.append(Classes.canonicalNameWithoutPackage(getClass()));
    }

}
