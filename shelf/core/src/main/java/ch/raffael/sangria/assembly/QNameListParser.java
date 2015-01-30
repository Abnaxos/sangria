package ch.raffael.sangria.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ch.raffael.sangria.libs.guava.collect.ImmutableSet;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
class QNameListParser {

    private final List<String> base;

    private final Tokenizer tokenizer;

    private final List<String> current = new ArrayList<>(8);

    private ImmutableSet.Builder<String> names = null;

    QNameListParser(List<String> base, String input) {
        tokenizer = new Tokenizer(input);
        this.base = base;
    }

    Set<String> qNameList() throws InvalidQNameListException {
        try {
            names = ImmutableSet.builder();
            expressionList(base != null && !base.isEmpty());
            if ( tokenizer.type() != Tokenizer.Type.EOI ) {
                throw tokenizer.unexpectedToken();
            }
            return names.build();
        }
        finally {
            names = null;
        }
    }

    private void expressionList(boolean allowRelative) throws InvalidQNameListException {
        do {
            String[] backup = current.toArray(new String[current.size()]);
            expression(allowRelative);
            current.clear();
            current.addAll(Arrays.asList(backup));
        } while ( tokenizer.type() == Tokenizer.Type.COMMA );
    }

    private void expression(boolean allowRelative) throws InvalidQNameListException {
        boolean allowThis = allowRelative;
        boolean allowSuper = allowRelative;
        tokenizer.next();
        if ( !tokenizer.type().namePart() ) {
            throw tokenizer.error("Name expected");
        }
        while ( true ) {
            switch ( tokenizer.type() ) {
                case IDENTIFIER:
                    allowThis = false;
                    allowSuper = false;
                    current.add(tokenizer.text());
                    break;
                case THIS:
                    if ( !allowThis ) {
                        throw tokenizer.unexpectedToken();
                    }
                    allowThis = allowSuper = false;
                    current.addAll(base);
                    break;
                case SUPER:
                    if ( !allowSuper ) {
                        throw tokenizer.unexpectedToken();
                    }
                    allowThis = false;
                    if ( current.isEmpty() ) {
                        current.addAll(base.subList(0, base.size() - 1));
                    }
                    else if ( current.size() == 1 ) {
                        throw tokenizer.error("super beyond package hierarchy");
                    }
                    else {
                        current.remove(current.size() - 1);
                    }
                    break;
                default:
                    consumeName();
                    return;
            }
            switch ( tokenizer.next() ) {
                case DOT:
                    // NOP
                    break;
                case PLUS:
                    consumeName();
                    break;
                default:
                    consumeName();
                    return;
            }
            if ( tokenizer.next().equals(Tokenizer.Type.PAREN_OPEN) ) {
                expressionList(false);
                if ( tokenizer.type() == Tokenizer.Type.PAREN_CLOSE ) {
                    tokenizer.next();
                }
                else {
                    throw tokenizer.error("Closing parenthesis expected");
                }
                return;
            }
            else if ( !tokenizer.type().namePart() ) {
                throw tokenizer.unexpectedToken();
            }
        }
    }

    private void consumeName() {
        names.add(String.join(".", current));
    }

    static class Tokenizer {

        private final String input;
        private int position = 0;
        private int line = 1;
        private int lineStart = 0;

        private Type type;
        private String text;
        private String errorContextHint = null;

        Tokenizer(String input) {
            this.input = input;
        }

        Type next() throws InvalidQNameListException {
            return next((Type[])null);
        }

        Type next(Type... expectedTypes) throws InvalidQNameListException {
            if ( type == Type.EOI ) {
                return Type.EOI;
            }
            skipSpaces();
            if ( eoi() ) {
                return set(Type.EOI, "", expectedTypes);
            }
            else {
                char c = input.charAt(position++);
                switch ( c ) {
                    case '+':
                        return set(Type.PLUS, "+", expectedTypes);
                    case '.':
                        return set(Type.DOT, ".", expectedTypes);
                    case '(':
                        return set(Type.PAREN_OPEN, "(", expectedTypes);
                    case ')':
                        return set(Type.PAREN_CLOSE, ")", expectedTypes);
                    case ',':
                        return set(Type.COMMA, ",", expectedTypes);
                    default:
                        if ( Character.isJavaIdentifierStart(c) ) {
                            int start = position - 1;
                            while ( !eoi() && Character.isJavaIdentifierPart(input.charAt(position)) ) {
                                position++;
                            }
                            String ident = input.substring(start, position);
                            switch ( ident ) {
                                case "this":
                                    return set(Type.THIS, ident, expectedTypes);
                                case "super":
                                    return set(Type.SUPER, ident, expectedTypes);
                                default:
                                    return set(Type.IDENTIFIER, ident, expectedTypes);
                            }
                        }
                        else {
                            throw error("Unexpected token '" + c + "'");
                        }
                }
            }
        }

        Type type() {
            return type;
        }

        String text() {
            return text;
        }

        InvalidQNameListException error(String message) {
            int charInLine = position - lineStart;
            return new InvalidQNameListException(
                    line, charInLine,
                    "Error at " + line + ":" + charInLine
                            + (errorContextHint == null ? "" : " (near '" + errorContextHint + "')")
                            + ": " + message);
        }

        InvalidQNameListException unexpectedToken() {
            String t;
            if ( type() == Type.EOI ) {
                t = "<end of input>";
            }
            else {
                t = text();
            }
            return error("Unexpected token '" + t + "'");
        }

        private Type set(Type type, String text, Type[] expectedTypes) throws InvalidQNameListException {
            if ( type == Type.IDENTIFIER ) {
                errorContextHint = text;
            }
            this.type = type;
            this.text = text;
            if ( expectedTypes != null && !Arrays.asList(expectedTypes).contains(type) ) {
                throw unexpectedToken();
            }
            return type;
        }

        private void skipSpaces() {
            boolean hadNewline = false;
            while ( !eoi() && Character.isWhitespace(input.charAt(position)) ) {
                if ( input.charAt(position) == '\n' ) {
                    hadNewline = true;
                    line++;
                    lineStart = position;
                }
                else if ( input.charAt(position) == '\r' ) {
                    if ( !hadNewline ) {
                        line++;
                    }
                    lineStart = position;
                    hadNewline = false;
                }
                else {
                    hadNewline = false;
                }
                position++;
            }
        }

        private boolean eoi() {
            return position >= input.length();
        }

        static enum Type {
            IDENTIFIER(true), THIS(true), SUPER(true), DOT, PLUS, PAREN_OPEN, PAREN_CLOSE, COMMA, EOI;

            private final boolean namePart;

            Type() {
                this(false);
            }

            Type(boolean namePart) {
                this.namePart = namePart;
            }

            boolean namePart() {
                return namePart;
            }

        }

    }

}
