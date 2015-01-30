package ch.raffael.sangria.experiments;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class StreamHandler {

    public static void main(String[] args) throws Exception {
        ClassLoader loader = new URLClassLoader(new URL[] { new URL("file:/tmp?blubb") }, StreamHandler.class.getClassLoader(), new MyUrlStreamHandlerFactory());
        loader.loadClass("foo.Bar");
    }

    public static class MyUrlStreamHandlerFactory implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            try {
                return new URLStreamHandlerWrapper((URLStreamHandler)Class.forName("sun.net.www.protocol." + protocol + ".Handler").newInstance());
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
            throw new InternalError("Could not load handler for protocol " + protocol);
        }
    }

    public static class URLStreamHandlerWrapper extends URLStreamHandler {

        private static final Method openConnection;
        private static final Method openConnectionWithProxy;
        static {
            try {
                openConnection = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                openConnectionWithProxy = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);
                openConnection.setAccessible(true);
                openConnectionWithProxy.setAccessible(true);
            }
            catch ( Exception e ) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private final URLStreamHandler delegate;

        public URLStreamHandlerWrapper(URLStreamHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            try {
                return (URLConnection)openConnection.invoke(delegate, url);
            }
            catch ( Exception e ) {
                throw new IOException("Error opening connection to " + url, e);
            }
        }

        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
            try {
                return (URLConnection)openConnectionWithProxy.invoke(delegate, url, proxy);
            }
            catch ( Exception e ) {
                throw new IOException("Error opening connection to " + url, e);
            }
        }
    }

}
