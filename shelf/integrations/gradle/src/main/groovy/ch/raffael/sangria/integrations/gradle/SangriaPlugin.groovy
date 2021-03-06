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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class SangriaPlugin implements Plugin<Project> {

    static final String BUNDLE_EXT = 'bnd'
    static final String BUNDLE_ARCHIVE_EXT = BUNDLE_EXT + '.zip'

    @Override
    void apply(Project project) {
        project.extensions.add('bundle', new BundleExtension(project))
        setupConfigurations(project)
        setupTasks(project)

        project.artifacts {
            archives project.tasks.bundleArchive
        }
    }

    private setupConfigurations(Project project) {
        project.configurations {
            bundleApi
            bundleSpi {
                extendsFrom bundleApi
            }
            bundleImpl {
                extendsFrom bundleSpi
            }
        }
    }

    private void setupTasks(Project project) {
        def bundleTask = project.task('bundle', type: Copy) {
//            extension = BUNDLE_ARCHIVE_EXT
            destinationDir = project.bundle.bundleDir
        }

        def api = project.configurations.bundleApi
        def spi = project.configurations.bundleSpi
        def impl = project.configurations.bundleImpl
        bundleTask.into('api') {
            from api
        }
        bundleTask.into('spi') {
            from (spi - api)
        }
        bundleTask.into('impl') {
            from (impl - spi)
        }

        def propertiesFile = project.file("$project.bundle.bundleDir/bundle.properties")
        bundleTask.doFirst({
            project.mkdir(project.bundle.bundleDir)
            Properties properties = new Properties()
            properties.setProperty('id', project.bundle.getId())
            properties.setProperty('version', project.version as String)
            if ( project.bundle.description != null ) {
                properties.setProperty('description', project.bundle.description)
            }
            propertiesFile.withOutputStream() { os ->
                properties.store(os, 'Generated by Sangria Gradle plugin')
            }
        })
        bundleTask.from propertiesFile

        def archiveTask = project.task('bundleArchive', type:Zip, dependsOn:bundleTask) {
            extension = BUNDLE_ARCHIVE_EXT
            destinationDir = project.bundle.bundleArchiveDir
            from project.bundle.bundleDir
        }
    }
}
