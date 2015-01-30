package ch.raffael.sangria.util;

import java.util.Optional;

import ch.raffael.sangria.libs.guava.reflect.TypeToken;


/**
 * Groovy has some bugs when parsing generic declarations. Use Java for such cases.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class TypeTokenWorkarounds {

    /**
     * Groovy parses `new TypeToken<Optional<? extends Number[]>>(){}` as `new TypeToken<Optional[]>(){}`
     */
    public static TypeToken<Optional<? extends Number[]>> optionalOfNumberArray() {
        return new TypeToken<Optional<? extends Number[]>>() {};
    }

}
