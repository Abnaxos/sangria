About
=====

Sangria was originally meant to be a DI container based on Guice, introducing several features:

 *  ClassLoader management similar to OSGi

 *  Cluster and module discovery at startup time.

 *  A two-phase startup mechanism that creates a Guice injector to configure the runtime injector. This enables injection of configuration components into the runtime modules

 *  An environment abstraction tailored for continuous delivery.

 *  Lots of useful utilities.

Because lots of the code had no dependency on Guice whatsoever, I decided to split the project into lots of small modules that may (nd probably will) be used independently. The project will probably be renamed, as most of the code is not related to Guice anymore. It's even possible that the container will be dropped entirely.

Licensed under the MIT license. See LICENSE.txt.


Components
==========

Alpha
-----

Components in Alpha state. You may already use them, but don't complain if something doesn't work or features are missing. They may also undergo incompatible refactorings and changes.

 *  **commons:** Some common code snippets.

 *  **logging:** *(may be merged into commons)* An extensible implementation of `org.slf4j.LoggerFactory`

 *  **dynamic:** Dynamic generation of classes. Used for proxying, bridging, etc. at framework level.

 *  **eventbus:** A parallel event bus that uses *dynamic* to call the handlers and other neat features.


Drafts
------

Draft projects are just that: Drafts. They contain code that may be used later on. Some of this code ist also just used to specify what will be done at some point. They may be subject to heavy changes.

 *  **manifest:** Allows to create Manifest entries using annotations and an annotation processor.

 *  **traits:** Groovy-Traits for Java using *dynamic*.


Planned
-------

 *  **builder:** Declarative way of creating builders. Also used to decorate constructors while keeping the source code compilable and without requiring any runtime instrumentation of this code. Needed for *traits*.

 *  **guards:** Dynamic guards. It will actually be an import of a [different Project)(https://github.com/Abnaxos/guards) and consist of two parts: The annotations themselves and a Java agent that will instrument the code accordingly. Probably an IDEA plugin, too.

 *  **annotation-index:** Provides a fast way to create an index of classes annotated with certain annotations and locate them at runtime.

 *  **cluster:** A file format that allows to combine several JAR files in one archive and load classes and resources from them.

 *  **cluster-gradle:** A Gradle plugin to produce cluster files.

 *  **guice-ext:** Some Guice extensions like indy-construct, lifecycle, warmup.

 *  **assembly:** The DI container.

 *  **environment:** The DI container will provide a sophisticated system for working with the environment the assembly is running in. This functionallity or parts of it may also be extracted to it's own component, if possible.


Shelf
-----

Code that has been written but will be deleted or moved. Some of this code is subject to be reused in planned components. I just don't want to delete these things yet.


Development
===========

Annotations
-----------

There are two annotations in `-commons` (package `ch.raffael.sangria.commons.annotations.development`) to mark code:

 *  **`Future`:** Will be implemented in the near future, don't use yet.

 *  **`Questionable`:** Somethings wrong with it -- expect this to change drastically or disappear entirely.

These annotations may appear at any code element.


IDEA
----

For those running IDEA with Java7 (that would be most people): For project synchronisation with Gradle, IDEA runs Gradle with the JVM it's running in. Setting properties in gradle.properties or in the global Gradle options won't change this. Unfortunately, this will cause the synchronisation to fail because Java7 doesn't understand Java8 bytecode. You'll have to put the following line into */path/to/idea/bin/idea.properties* to make it work:

    org.gradle.java.home=/path/to/jdk1.8.0

This will, however, cause IDEA to use that setting globally. I've never had any problem with this, but I can't rule it out.
