package ch.raffael.sangria.environment;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface EnvironmentBuilder extends Environment {

    EnvironmentBuilder set(String key, String value);

    EnvironmentBuilder forPrefix(String prefix);

}
