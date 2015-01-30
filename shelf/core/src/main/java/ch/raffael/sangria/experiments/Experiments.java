package ch.raffael.sangria.experiments;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
//@ExtensionPoint
public class Experiments extends Mod {

    @Override
    protected void configure() {
        super.configure();
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                //encounter.addError("My error");
                System.out.println(type);
                //encounter.register();
                System.out.println(encounter);
            }
        });
        Foo foo = new Foo();
        foo.foo = "bar";
        bind(Foo.class).annotatedWith(Annot.class).toInstance(foo);
        //bind(Foo.class);
        bind(Bar.class);
    }

    public boolean equals(Object other) {
        if ( other == this ) {
            return true;
        }
        if ( !(other instanceof Experiments) ) {
            return false;
        }
        return this.equals(other);
    }

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new Experiments(), new Mod2());
        injector.getInstance(Bar.class).sayHello();
        injector.getInstance(Bar2.class).sayHello();
    }

    public static class Foo {

        private String foo;
        public Foo() {
        }

        @Override
        public String toString() {
            return "Foo:" + foo;
        }
    }

    public static class Bar {

        private final Foo foo;

        @Inject
        public Bar(Foo foo) {
            this.foo = foo;
        }

        public void sayHello() {
            System.out.println("Hello world: " + foo);
        }

    }

    public static class Bar2 {

        private final Foo foo;

        @Inject
        public Bar2(Foo foo) {
            this.foo = foo;
        }

        public void sayHello() {
            System.out.println("Hello world: " + foo);
        }

    }

    public static class Mod2 extends Mod {

        @Override
        protected void configure() {
            super.configure();
            //bind(Bar2.class);
        }
    }

}
