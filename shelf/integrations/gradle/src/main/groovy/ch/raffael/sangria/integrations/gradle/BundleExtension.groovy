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

import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySet
import org.gradle.util.ConfigureUtil


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class BundleExtension {

    final Project bundleProject
    final Deps deps = new Deps()

    private Object id = null
    private Object description = null

    private Object destinationDir = null
    private Object bundleDir = null
    private Object bundleArchiveDir = null

    BundleExtension(Project bundleProject) {
        this.bundleProject = bundleProject
    }

    void setId(Object id) {
        this.@id = id
    }

    Object getId() {
        if ( this.@id == null ) {
            return "${bundleProject.group}.${bundleProject.archivesBaseName}"
        }
        else {
            return this.@id as String
        }
    }

    String getDescription() {
        if ( this.@description == null ) {
            return null
        }
        else {
            return this.@description as String
        }
    }

    void setDescription(Object description) {
        this.@description = description
    }

    void setDestinationDir(Object workDir) {
        this.@destinationDir = workDir
    }

    File getDestinationDir() {
        return bundleProject.file(this.@destinationDir ?: "$bundleProject.buildDir/sangria-bundle")
    }

    Object getBundleDir() {
        return bundleProject.file(this.@bundleDir ?: "${getDestinationDir()}/${bundleProject.archivesBaseName}.$SangriaPlugin.BUNDLE_EXT")
    }

    void setBundleDir(Object bundleDir) {
        this.@bundleDir = bundleDir
    }

    Object getBundleArchiveDir() {
        return bundleProject.file(this.@bundleArchiveDir ?: getDestinationDir())
    }

    void setBundleArchiveDir(Object bundleArchive) {
        this.@bundleArchiveDir = bundleArchive
    }

    void api(String name, Closure<?> closure=null) {
        api(bundleProject.project(name), closure)
    }

    void api(Project prj, Closure<?> closure=null) {
        bundleProject.dependencies {
            bundleApi prj
        }
        if ( closure ) {
            bundleProject.project(prj.path, closure)
        }
    }

    void spi(String name, Closure<?> closure=null) {
        spi(bundleProject.project(name), closure)
    }

    void spi(Project prj, Closure<?> closure=null) {
        bundleProject.dependencies {
            bundleSpi prj
        }
        prj.dependencies {
            compile deps.api
        }
        if ( closure ) {
            bundleProject.project(prj.path, closure)
        }
    }

    void impl(String name, Closure<?> closure=null) {
        impl(bundleProject.project(name), closure)
    }

    void impl(Project prj, Closure<?> closure=null) {
        bundleProject.dependencies {
            bundleImpl prj
        }
        prj.dependencies {
            compile deps.spi
        }
        if ( closure ) {
            bundleProject.project(prj.path, closure)
        }
    }

    void call(Closure closure) {
        ConfigureUtil.configure(closure, this)
    }

    class Deps {

        DependencySet getApi() {
            return api()
        }

        DependencySet api() {
            bundleProject.configurations.bundleApi.allDependencies
        }

        DependencySet getSpi() {
            return spi()
        }

        DependencySet spi() {
            bundleProject.configurations.bundleSpi.allDependencies
        }

        DependencySet getImpl() {
            return impl()
        }

        DependencySet impl() {
            bundleProject.configurations.bundleImpl.allDependencies
        }

    }

}
