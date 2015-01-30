package ch.raffael.sangria.ext;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Completer {

    <T> T substantiate(Class<T> type);

}
