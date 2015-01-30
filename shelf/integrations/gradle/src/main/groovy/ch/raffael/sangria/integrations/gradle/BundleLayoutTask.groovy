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

package ch.raffael.sangria.integrations.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class BundleLayoutTask extends DefaultTask {

    @TaskAction
    void writeLayoutFile() {
        def output = project.file("$project.bundle.workDir/layout")
        Set seen = []
        println 'API'
        for ( dep in project.configurations.bundleApi.allDependencies ) {
            handle(dep)
        }
        println 'SPI'
        for ( dep in project.configurations.bundleSpi.allDependencies ) {
            handle(dep)
        }
        println 'IMPL'
        for ( dep in project.configurations.bundleImpl.allDependencies ) {
            handle(dep)
        }
    }

    private void handle(Dependency dep) {
        if ( dep instanceof ProjectDependency ) {
            dep.dependencyProject.jar.inputs.each { println it }
        }
        else {
            println dep.file
        }
    }

}
