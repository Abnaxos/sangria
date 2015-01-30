package ch.raffael.sangria.assembly;

import java.util.regex.Pattern;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Validators {

    private static final String JAVA_IDENT_RE_SRC = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    private static final Pattern JAVA_IDENTIFIER = Pattern.compile(JAVA_IDENT_RE_SRC);
    private static final Pattern JAVA_QUALIFIED_IDENTIFIER = Pattern.compile(JAVA_IDENT_RE_SRC + "(\\." + JAVA_IDENT_RE_SRC + ")*");

    private Validators() {
    }

    public static boolean isIdentifier(String string) {
        return JAVA_IDENTIFIER.matcher(string).matches();
    }

    public static boolean isQualifiedIdentifier(String string) {
        return JAVA_QUALIFIED_IDENTIFIER.matcher(string).matches();
    }

    public static void checkIdentifier(String string) {
        if ( !isIdentifier(string) ) {
            throw new IllegalArgumentException("Not a valid identifier: " + string);
        }
    }

    public static void checkQualifiedIdentifier(String string) {
        if ( !isQualifiedIdentifier(string) ) {
            throw new IllegalArgumentException("Not a valid package name: " + string);
        }
    }

}
