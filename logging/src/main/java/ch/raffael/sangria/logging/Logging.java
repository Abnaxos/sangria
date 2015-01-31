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
import ch.raffael.sangria.commons.Suppliers;


/**
 * Replacement for `org.slf4j.LoggerFactory`. It may apply some modifications to the logger return
 * based on annotations or caused by plugins.
 *
 * It also uses {@link ClassValue} to cache loggers.
 *
 * @todo Does it really make sense to use {@link ClassValue} for caching loggers?
 * Loggers are usually static, after all, the possible slowdown for overusing {@link ClassValue}
 * may overweight the minor performance loss doing the necessary calculations for creating a new
 * logger.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Logging {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = loggingLogger();

    private static final ClassValue<LoggerEntry> LOGGERS = new ClassValue<LoggerEntry>() {
        @Override
        protected LoggerEntry computeValue(Class<?> type) {
            return new LoggerEntry(type);
        }
    };

    private Logging() {
    }

    /**
     * Returns a logger for the calling class using the outermost class in case of inner classes.
     * If you need a logger specifically for the inner class, use {@link #logger(Class)}
     *
     * @return A `org.slf4j.Logger`.
     */
    public static Logger logger() {
        return LOGGERS.get(Classes.componentType(Classes.callerClass(Logging.class))).implicit.get();
    }

    /**
     * Returns a logger for the specified class.
     *
     * @param realType    The class.
     *
     * @return A `org.slf4j.Logger`.
     */
    public static Logger logger(Class<?> type) {
        return LOGGERS.get(Classes.componentType(type)).explicit.get();
    }

    private static Logger LOGGING_LOGGER = LoggerFactory.getLogger(Logger.class);
    static Logger loggingLogger() {
        return LOGGING_LOGGER;

    }

    private static class LoggerEntry {
        private final Class<?> type;
        final Supplier<Logger> implicit;
        final Supplier<Logger> explicit;
        LoggerEntry(Class<?> type) {
            this.type = type;
            implicit = Suppliers.lazy(() -> LoggerFactory.getLogger(loggerNameForClass(Classes.outermostClass(this.type))));
            explicit = Suppliers.lazy(() -> LoggerFactory.getLogger(loggerNameForClass(this.type)));
        }
        private static String loggerNameForClass(Class<?> type) {
            assert !type.isArray();
            Class<?> loggerType = type;
            while ( true ) {
                String name = loggerType.getCanonicalName();
                if ( name != null ) {
                    return name;
                }
                else {
                    // it's an anonymous or local class; try the outer class instead
                    if ( loggerType.getEnclosingClass() != null) {
                        loggerType = loggerType.getEnclosingClass();
                    }
                    else {
                        // fall back to the original class' "normal" class name
                        return type.getName();
                    }
                }
            }
        }
    }

}
