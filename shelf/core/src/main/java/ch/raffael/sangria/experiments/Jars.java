package ch.raffael.sangria.experiments;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ch.raffael.sangria.libs.guava.base.Stopwatch;
import ch.raffael.sangria.libs.guava.io.ByteStreams;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Jars {

    public static final String CLASSFLILE = "/com/intellij/testFramework/fixtures/impl/JavaModuleFixtureBuilderImpl.class";
    public static final String JARFILE = "openapi.jar";

    public static void main(String[] args) throws Exception {
        nested();
    }

    public static void standalone() throws Exception {
        Path path = Paths.get("/opt/idea/lib/openapi.jar");
        FileSystem jarfs = FileSystems.newFileSystem(path, null);
        Stopwatch sw = Stopwatch.createStarted();
        for ( int i = 0; i < 1_000_000; i++ ) {
            try ( InputStream input = Files.newInputStream(jarfs.getPath(CLASSFLILE)) ) {
                ByteStreams.toByteArray(input);
            }
        }
        sw.stop();
        System.out.println(sw);
    }

    public static void nested() throws Exception {
        Path path = Paths.get("/home/rherzog/Temp/idea-lib.jar");
        FileSystem outer = FileSystems.newFileSystem(path, null);
        FileSystem jarfs = FileSystems.newFileSystem(outer.getPath("/openapi.jar"), null);
        Stopwatch sw = Stopwatch.createStarted();
        for ( int i = 0; i < 1_000_000; i++ ) {
            try ( InputStream input = Files.newInputStream(jarfs.getPath(CLASSFLILE)) ) {
                ByteStreams.toByteArray(input);
            }
        }
        sw.stop();
        System.out.println(sw);
    }

}
