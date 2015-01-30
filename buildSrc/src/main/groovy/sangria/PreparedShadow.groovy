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

package sangria

import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.ide.idea.IdeaPlugin


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class PreparedShadow implements Plugin<Project> {

    static final String JAR_TASK = 'preparedShadowJar'
    static final String SOURCE_JAR_TASK = 'preparedShadowSourceJar'
    static final String WORKDIR = 'prepared-shadow'
    static final String CONFIGURATION = 'shadowed'
    static final String APPENDIX = 'SHADOWED'

    @Override
    void apply(Project project) {
        project.with {
            configurations.create(CONFIGURATION)

            task(JAR_TASK, type:ShadowJar) {
                destinationDir = file("$buildDir/$WORKDIR")
                appendix = APPENDIX
                configurations = [project.configurations.getByName(CONFIGURATION)]
                version = null
                includeEmptyDirs = false

                // todo: merging etc.
                exclude 'META-INF/**/*'
            }

            task(SOURCE_JAR_TASK, type: Jar) {
                outputs.upToDateWhen { false }
                destinationDir = file("$buildDir/$WORKDIR")
                appendix = APPENDIX
                classifier = 'sources'
                version = null
                includeEmptyDirs = false
            }
            afterEvaluate {
                def task = tasks[SOURCE_JAR_TASK]
                def jarTask = tasks[JAR_TASK]
                configurations.detachedConfiguration(
                        configurations[CONFIGURATION].allDependencies.collect({ dep ->
                            dependencies.create(group: dep.group,
                                                name: dep.name,
                                                classifier: 'sources',
                                                version: dep.version)
                        }) as Dependency[]).each { f -> task.configure { from f.directory ? fileTree(f) : zipTree(f) }}
                task.eachFile { FileCopyDetails details ->
                    if ( details.directory ) {
                        return
                    }
                    jarTask.relocators.each { Relocator reloc ->
                        task.configure {
                            if ( reloc.canRelocatePath(details.sourcePath) ) {
                                details.path = reloc.relocatePath(details.sourcePath)
                                details.filter { line -> reloc.applyToSourceContent(line) }
                            }
                        }
                    }
                }
            }
            sourceSets.main.compileClasspath += files(tasks[JAR_TASK].archivePath)
            sourceSets.test.compileClasspath += files(tasks[JAR_TASK].archivePath)
            sourceSets.test.runtimeClasspath += files(tasks[JAR_TASK].archivePath)

            if ( plugins.findPlugin(JavaPlugin) ) {
                apply plugin:PreparedShadowJava
            }
            if ( plugins.findPlugin(GroovyPlugin) ) {
                apply plugin:PreparedShadowGroovy
            }
            if ( plugins.findPlugin(IdeaPlugin) ) {
                apply plugin:PreparedShadowIdea
            }
        }

    }
}
