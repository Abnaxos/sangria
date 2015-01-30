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

package ch.raffael.sangria.traits;

import ch.raffael.sangria.dynamic.ClassSynthesizer;
import ch.raffael.sangria.dynamic.asm.Type;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Synthesizer extends ClassSynthesizer {

    private static final ClassValue<Synthesizer> INSTANCES = new ClassValue<Synthesizer>() {
        @Override
        protected Synthesizer computeValue(Class type) {
            return new Synthesizer(type);
        }
    };

    private final Class<?> traitTarget;
    private final ConstructorGenerator constructorGenerator;


    private Synthesizer(Class<?> traitTarget) {
        super(traitTarget.getClassLoader());
        this.traitTarget = traitTarget;
        constructorGenerator = new ConstructorGenerator();
        linkGenerator(constructorGenerator);
    }

    static Synthesizer instance(Class<?> traitTarget) {
        return INSTANCES.get(traitTarget);
    }

    private class ConstructorGenerator extends ClassGenerator {

        /**
         * Constructor.
         *
         * @param targetType The ASM Type of the class that will be generated by this generator.
         */
        public ConstructorGenerator() {
            super(Type.getType(traitTarget));
        }

        @Override
        protected void generate() {
        }
    }

}