package ch.raffael.sangria.modules.lifecycle;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Action {

    void perform(Object target) throws Exception;

}
