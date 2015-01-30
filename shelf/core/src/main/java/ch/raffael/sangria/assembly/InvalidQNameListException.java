package ch.raffael.sangria.assembly;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class InvalidQNameListException extends Exception {

    private final int line;
    private final int charInLine;

    public InvalidQNameListException(int line, int charInLine, String message) {
        super(message);
        this.line = line;
        this.charInLine = charInLine;
    }

    public int getLine() {
        return line;
    }

    public int getCharInLine() {
        return charInLine;
    }
}
