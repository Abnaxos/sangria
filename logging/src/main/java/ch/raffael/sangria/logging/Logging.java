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

package ch.raffael.sangria.logging;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.raffael.sangria.commons.Classes;


/**
 * Replacement for `org.slf4j.LoggerFactory`. It may apply some modifications to the logger return
 * based on annotations or caused by plugins.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Logging {

    private Logging() {
    }

    /**
     * Returns a logger for the calling class using the outermost class in case of inner classes.
     * If you need a logger specifically for the inner class, use {@link #logger(Class)}
     *
     * @return A `org.slf4j.Logger`.
     */
    public static Logger logger() {
        return logger(outermost(Classes.callerClass(Logging.class)));
    }

    /**
     * Returns a logger for the specified class.
     *
     * @param clazz    The class.
     *
     * @return A `org.slf4j.Logger`.
     */
    public static Logger logger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    private static Class<?> outermost(Class<?> clazz) {
        while ( clazz.getEnclosingClass() != null ) {
            clazz = clazz.getEnclosingClass();
        }
        return clazz;
    }

    private static Logger LOGGING_LOGGER = ((Supplier<Logger>)() -> {
        String name = Logger.class.getName();
        int pos = name.lastIndexOf('.');
        return LoggerFactory.getLogger(name);
    }).get();
    static Logger loggingLogger() {
        return LOGGING_LOGGER;
    }

}
