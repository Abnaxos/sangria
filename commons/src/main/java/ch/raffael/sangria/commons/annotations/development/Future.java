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

package ch.raffael.sangria.commons.annotations.development;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotates any element that will be implemented in the future. Using them now can result in
 * arbitrary behaviour.
 *
 * This annotation is useful for:
 *
 *  *  informing the user that this element is part of something that's planned
 *  *  remembering plans an partially specifying them, enabling a usage search
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
//@Target({ElementType.TYPE) // applicable to anything
@Retention(RetentionPolicy.SOURCE)
@Repeatable(Future.List.class)
@Documented
public @interface Future {
    String value() default "";

    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @interface List {
        Future[] value();
    }
}
