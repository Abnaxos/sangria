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

package ch.raffael.sangria.dynamic

import ch.raffael.sangria.libs.guava.reflect.TypeToken
import spock.lang.Specification
import spock.lang.Unroll

import static ch.raffael.sangria.dynamic.TypeCasts.CastMethod.*
import static ch.raffael.sangria.dynamic.TypeCasts.Kind.*


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Unroll
@SuppressWarnings("GroovyAccessibility")
class TypeCastsSpec extends Specification {

    @SuppressWarnings(["UnnecessaryQualifiedReference", "GroovyAssignabilityCheck"])
    def "Cast kind: #sourceName ~> #targetName: #kind(#method)"() {
      when:
        def result = cast(source, target)

      then:
        result[0] == kind
        result[1] == method

      where:
        //@formatter:off
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        source                                       | target                                         || kind          | method
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        void                                         | int                                            || VOID          | TypeCasts.CastMethod.VOID
        int                                          | void                                           || VOID          | TypeCasts.CastMethod.VOID
        void                                         | void                                           || SAFE          | SIMPLE

        int                                          | long                                           || SAFE          | PRIM_PRIM
        int                                          | short                                          || UNSAFE        | PRIM_PRIM
        int                                          | double                                         || FLOAT_INT_MIX | PRIM_PRIM
        float                                        | double                                         || SAFE          | PRIM_PRIM
        double                                       | float                                          || UNSAFE        | PRIM_PRIM
        double                                       | int                                            || FLOAT_INT_MIX | PRIM_PRIM

        int                                          | Long                                           || SAFE          | PRIM_WRAPPER
        int                                          | Short                                          || UNSAFE        | PRIM_WRAPPER
        int                                          | Double                                         || FLOAT_INT_MIX | PRIM_WRAPPER
        float                                        | Double                                         || SAFE          | PRIM_WRAPPER
        double                                       | Float                                          || UNSAFE        | PRIM_WRAPPER
        double                                       | Integer                                        || FLOAT_INT_MIX | PRIM_WRAPPER
        short                                        | Number                                         || SAFE          | PRIM_WRAPPER
        boolean                                      | Object                                         || SAFE          | PRIM_WRAPPER
        int                                          | String                                         || null          | null
        boolean                                      | Boolean                                        || SAFE          | PRIM_WRAPPER
        boolean                                      | Integer                                        || null          | null

        Integer                                      | int                                            || SAFE          | WRAPPER_PRIM
        Boolean                                      | boolean                                        || SAFE          | WRAPPER_PRIM
        Boolean                                      | int                                            || null          | null
        Integer                                      | long                                           || SAFE          | WRAPPER_PRIM
        Integer                                      | short                                          || UNSAFE        | WRAPPER_PRIM
        Integer                                      | double                                         || FLOAT_INT_MIX | WRAPPER_PRIM
        Float                                        | double                                         || SAFE          | WRAPPER_PRIM
        Double                                       | float                                          || UNSAFE        | WRAPPER_PRIM
        Double                                       | int                                            || FLOAT_INT_MIX | WRAPPER_PRIM

        Integer                                      | Integer                                        || SAFE          | SIMPLE
        Integer                                      | Short                                          || UNSAFE        | WRAPPER_WRAPPER
        Integer                                      | Double                                         || FLOAT_INT_MIX | WRAPPER_WRAPPER
        Float                                        | Double                                         || SAFE          | WRAPPER_WRAPPER
        Double                                       | Float                                          || UNSAFE        | WRAPPER_WRAPPER
        Double                                       | Integer                                        || FLOAT_INT_MIX | WRAPPER_WRAPPER
        Boolean                                      | Integer                                        || null          | null

        Integer                                      | Number                                         || SAFE          | SIMPLE
        Number                                       | Integer                                        || DOWNCAST      | OBJECT
        Integer                                      | String                                         || null          | null

        Integer[][].class                            | Object[].class                                 || SAFE          | SIMPLE
        Integer[][].class                            | Integer[].class                                || null          | null
        Integer[][].class                            | Integer[][].class                              || SAFE          | SIMPLE

        new TypeToken<Optional<Integer>>() {}        | new TypeToken<Optional<? extends Number>>() {} || SAFE          | SIMPLE
        new TypeToken<Optional<Number[]>>() {}       | new TypeToken<Optional<Integer[]>>() {}        || null          | null
        new TypeToken<Optional<Integer[]>>() {}      | TypeTokenWorkaround.optionalOfNumberArray()    || SAFE          | SIMPLE
//      new TypeToken<Optional<Integer[]>>() {}      | new TypeToken<Optional<Number[]>>() {}         || SAFE          | SIMPLE

        TypeTokenWorkaround.optionalOfNumberArray()  | new TypeToken<Optional<Integer[]>>() {}        || UNCHECKED     | OBJECT
//      new TypeToken<Optional<Number[]>>() {}       | new TypeToken<Optional<Integer[]>>() {}        || UNCHECKED     | OBJECT
        Optional                                     | new TypeToken<Optional<Integer>>() {}          || UNCHECKED     | OBJECT

        String                                       | Closeable                                      || null          | null
        Number                                       | Closeable                                      || DOWNCAST      | OBJECT
        Number                                       | Iterable                                       || UNCHECKED     | OBJECT
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //@formatter:on

        // capture the type names for beautiful feature names
        sourceName = source instanceof TypeToken ? source.type.typeName : source.typeName
        targetName = target instanceof TypeToken ? target.type.typeName : target.typeName
    }

    private static List cast(source, target) {
        source = token(source)
        target = token(target)
        try {
            return (List)TypeCasts.cast(EnumSet.allOf(TypeCasts.Kind), token(source), token(target), { m, k -> [k, m] } as TypeCasts.Cast)
        }
        catch ( IncompatibleTypesException e ) {
            println e
            return [null, null]
        }
    }

    private static TypeToken<?> token(from) {
        if ( from instanceof Class ) {
            return TypeToken.of((Class)from)
        }
        else if ( from instanceof TypeToken ) {
            return from
        }
        else {
            throw new Exception()
        }
    }

}
