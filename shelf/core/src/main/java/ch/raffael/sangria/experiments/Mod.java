package ch.raffael.sangria.experiments;

import com.google.inject.AbstractModule;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Mod extends AbstractModule {

    @Override
    protected void configure() {
        //binder().newPrivateBinder().bind(Experiments.Foo.class).toInstance(new Experiments.Foo(getClass().getSimpleName()));
    }
}
