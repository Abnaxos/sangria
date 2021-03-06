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

package ch.raffael.sangria.logging

import spock.lang.Specification


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class LoggingSpec extends Specification {

    def "logger() returns logger for caller if outermost class"() {
      given:
        def logger = LoggingClass.myLogger()

      expect:
        logger.name == LoggingClass.name
    }

    def "logger() returns logger for outermost class for inner classes"() {
      given:
        def logger = LoggingClass.InnerLoggingClass.myLogger()

      expect:
        logger.name == LoggingClass.name
    }

    def "logger(Class) returns logger for innermost named class on anonymous class"() {
      given:
        def logger = Logging.logger(LoggingClass.anonymousClass())

      expect:
        logger.name == LoggingClass.canonicalName
    }

    def "logger(Class) returns logger for innermost named class on local class"() {
      given:
        def logger = Logging.logger(LoggingClass.localClass())

      expect:
        logger.name == LoggingClass.canonicalName
    }

}
