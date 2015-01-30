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

package ch.raffael.sangria.dynamic;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public enum Visibility {
    PRIVATE("private"), PACKAGE(""), PROTECTED("protected"), PUBLIC("public");

    private final String modifierString;
    private final VisibilityPredicate predicate = new VisibilityPredicate();

    Visibility(String modifierString) {
        this.modifierString = modifierString;
    }

    public String modifierString() {
        return modifierString;
    }

    public static Visibility of(Member member) {
        return of(member.getModifiers());
    }

    public static Visibility of(int modifiers) {
        if ( Modifier.isPrivate(modifiers) ) {
            return PRIVATE;
        }
        else if ( Modifier.isProtected(modifiers) ) {
            return PROTECTED;
        }
        else if ( Modifier.isPublic(modifiers) ) {
            return PUBLIC;
        }
        else {
            return PACKAGE;
        }
    }

    public VisibilityPredicate predicate() {
        return predicate;
    }

    public class VisibilityPredicate implements Predicate<Member> {

        private Predicate<Member> orMore = (m) -> of(m).ordinal() >= ordinal();
        private Predicate<Member> orLess = (m) -> of(m).ordinal() <= ordinal();

        @Override
        public boolean test(Member member) {
            return of(member) == Visibility.this;
        }

        public Predicate<Member> orMore() {
            return orMore;
        }

        public Predicate<Member> orLess() {
            return orLess;
        }

        public Predicate<Member> to(Visibility other) {
            if ( other == Visibility.this ) {
                return this;
            }
            else if ( other.ordinal() > ordinal() ) {
                return (m) -> between(of(m).ordinal(), ordinal(), other.ordinal());
            }
            else {
                return (m) -> between(of(m).ordinal(), other.ordinal(), ordinal());
            }
        }

        private boolean between(int val, int min, int max) {
            return val >= min && val <= max;
        }

    }

}
