package ch.raffael.sangria.annotations.index;

import java.io.IOException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class IndexSyntaxException extends IOException {

    private final int line;

    public IndexSyntaxException(int line, String message) {
        super("Line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
