package ch.raffael.sangria.experiments;

import java.lang.reflect.Method;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Conf extends AbstractModule {

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new Conf());
        try {
            test(injector);
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static void test(Injector injector) {
        Bar bar = injector.getInstance(Bar.class);
        System.out.println();
        System.out.println(bar);
        bar.boo();
    }

    @Override
    protected void configure() {
        bindInterceptor(Matchers.subclassesOf(Provider.class), new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return method.getName().equals("get") && method.getParameterTypes().length == 0;
            }
        }, new MethodInterceptor() {
                            @Override
                            public Object invoke(MethodInvocation invocation) throws Throwable {
                                System.out.println("Intercept");
                                return invocation.proceed();
                            }
                        });
        //binder().requireExplicitBindings();
        bind(Foo.class);
    }

    public static class Foo {

        private String test = "foo";
    }

    public static class Bar {

        final Foo foo;

        @Inject
        public Bar(Foo foo) {
            this.foo = foo;
            System.out.println("ctor: " + foo.test);
        }

        public void boo() {
            System.out.println("boo: " + foo.test);
        }
    }

}
