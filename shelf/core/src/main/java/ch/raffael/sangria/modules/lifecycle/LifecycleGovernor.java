package ch.raffael.sangria.modules.lifecycle;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface LifecycleGovernor {

    boolean postConstruct(Object key, Object object);

    void preDestroy(Object key);

}
