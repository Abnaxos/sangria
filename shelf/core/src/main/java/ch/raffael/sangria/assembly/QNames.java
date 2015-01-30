package ch.raffael.sangria.assembly;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class QNames {

    private static final String JAVA_IDENT_RE_SRC = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern JAVA_IDENTIFIER = Pattern.compile(JAVA_IDENT_RE_SRC);

    public static final String SUPER = "super";
    public static final String THIS = "this";

    public static List<String> toList(String qName) {
        int partCount = 1;
        boolean empty = true;
        for ( int i = 0; i < qName.length(); i++ ) {
            if ( qName.charAt(i) == '.' ) {
                partCount++;
            }
            else if ( !empty || !Character.isWhitespace(qName.charAt(i)) ) {
                empty = false;
            }
        }
        if ( empty ) {
            return Collections.emptyList();
        }
        String[] parts = new String[partCount];
        int partIndex = 0;
        int partStart = 0;
        for ( int i = 0; i < qName.length(); i++ ) {
            if ( qName.charAt(i) == '.' ) {
                parts[partIndex++] = qName.substring(partStart, i);
                partStart = i + 1;
            }
        }
        parts[partIndex] = qName.substring(partStart, qName.length());
        for ( int i = 0; i < parts.length; i++ ) {
            String part = parts[i].trim();
            if ( !JAVA_IDENTIFIER.matcher(part).matches() || part.equals(SUPER) || part.equals(THIS) ) {
                throw new IllegalArgumentException("Invalid qualified Java name: '" + qName + "'");
            }
            parts[i] = part;
        }
        return Collections.unmodifiableList(Arrays.asList(parts));
    }

    @SuppressWarnings("RedundantIfStatement")
    public static Set<String> parseQNameList(String base, String qNameList) throws InvalidQNameListException {
        List<String> baseList = null;
        if ( base != null && !base.trim().isEmpty() ) {
            baseList = toList(base);
        }
        return new QNameListParser(baseList, qNameList).qNameList();
    }

    //public static Set<String> parseQNameList(String base, String qNameList) throws InvalidQNameListException {
    //    ErrorCollector errorCollector = new ErrorCollector();
    //    QNameListLexer lexer = new QNameListLexer(new ANTLRInputStream(qNameList));
    //    lexer.removeErrorListeners();
    //    lexer.addErrorListener(errorCollector);
    //    QNameListParser2 parser = new QNameListParser2(new CommonTokenStream(lexer));
    //    parser.removeErrorListeners();
    //    parser.addErrorListener(errorCollector);
    //    boolean hasBase;
    //    if ( base == null || base.trim().isEmpty() ) {
    //        hasBase = false;
    //    }
    //    else {
    //        hasBase = true;
    //    }
    //    QNameListParser2.QNameListContext tree = parser.qNameList(hasBase);
    //    errorCollector.throwErrors();
    //    QNameListVisitor visitor =  new QNameListVisitor(hasBase ? toList(base) : Collections.<String>emptyList(), errorCollector);
    //    visitor.visit(tree);
    //    errorCollector.throwErrors();
    //    return visitor.getResult();
    //}
    //
    //private static abstract class AbstractQNameListVisitor<T> extends QNameListBaseVisitor<Boolean> {
    //
    //    protected final ErrorCollector errorCollector;
    //    private final List<String> base;
    //    private final List<String> current = new ArrayList<>();
    //
    //    private AbstractQNameListVisitor(List<String> base, ErrorCollector errorCollector) {
    //        this.errorCollector = errorCollector;
    //        this.base = base;
    //    }
    //
    //    @Override
    //    public Boolean visitExpression(@NotNull QNameListParser2.ExpressionContext ctx) {
    //        boolean result = visitChildren(ctx);
    //        if ( result && ctx.name() == null && ctx.group() == null ) {
    //            QNameListParser2.RelativeContext relative = ctx.relative();
    //            if ( relative.relThis != null ) {
    //                addCurrent(relative.relThis);
    //            }
    //            else if ( !relative.relSuper.isEmpty() ) {
    //                addCurrent(relative.relSuper.get(0));
    //            }
    //        }
    //        current.clear();
    //        return result;
    //    }
    //
    //    @Override
    //    public Boolean visitRelative(@NotNull QNameListParser2.RelativeContext ctx) {
    //        current.addAll(base);
    //        return visitChildren(ctx);
    //    }
    //
    //    @Override
    //    public Boolean visitTerminal(@NotNull TerminalNode node) {
    //        if ( node.getSymbol().getType() == QNameListParser2.SUPER ) {
    //            if ( current.isEmpty() ) {
    //                errorCollector.report(node, "super out of package hierarchy");
    //                return false;
    //            }
    //            else {
    //                current.remove(current.size() - 1);
    //            }
    //        }
    //        return true;
    //    }
    //
    //    @Override
    //    public Boolean visitName(@NotNull QNameListParser2.NameContext ctx) {
    //        current.add(ctx.ID().getText());
    //        if ( ctx.cont() == null ) {
    //            addCurrent(ctx.ID().getSymbol());
    //        }
    //        boolean result = visitChildren(ctx);
    //        current.remove(current.size() - 1);
    //        return result;
    //    }
    //
    //    @Override
    //    public Boolean visitSeparator(@NotNull QNameListParser2.SeparatorContext ctx) {
    //        if ( ctx.inclusive ) {
    //            addCurrent(ctx.sep);
    //        }
    //        return visitChildren(ctx);
    //    }
    //
    //    protected String currentName() {
    //        StringBuilder buf = new StringBuilder();
    //        for ( String part : current ) {
    //            if ( buf.length() > 0 ) {
    //                buf.append('.');
    //            }
    //            buf.append(part);
    //        }
    //        return buf.toString();
    //    }
    //
    //    @Override
    //    protected Boolean defaultResult() {
    //        return true;
    //    }
    //
    //    @Override
    //    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
    //        return aggregate && nextResult;
    //    }
    //
    //    protected abstract void addCurrent(Token token);
    //
    //    public abstract Set<T> getResult();
    //}
    //
    //private static class QNameListVisitor extends AbstractQNameListVisitor<String> {
    //
    //    private final Set<String> names = new LinkedHashSet<>();
    //
    //    private QNameListVisitor(List<String> base, ErrorCollector errorCollector) {
    //        super(base, errorCollector);
    //    }
    //
    //    @Override
    //    protected void addCurrent(Token token) {
    //        String name = currentName();
    //        if ( name.isEmpty() ) {
    //            errorCollector.report(token, "Cannot reference default package");
    //        }
    //        else {
    //            names.add(name);
    //        }
    //    }
    //
    //    @Override
    //    public Set<String> getResult() {
    //        return Collections.unmodifiableSet(names);
    //    }
    //}
    //
    //private static class ErrorCollector extends BaseErrorListener {
    //
    //    private final List<Error> errors = new ArrayList<>();
    //
    //    @Override
    //    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    //        report(line, charPositionInLine, msg);
    //    }
    //
    //    public void report(int line, int charInLine, String message) {
    //        errors.add(new Error(line, charInLine, message));
    //    }
    //
    //    public void report(Token token, String message) {
    //        errors.add(new Error(token.getLine(), token.getCharPositionInLine(), message));
    //    }
    //
    //    public void report(TerminalNode node, String message) {
    //        errors.add(new Error(node.getSymbol(), message));
    //    }
    //
    //    public void throwErrors() throws InvalidQNameListException {
    //        if ( !errors.isEmpty() ) {
    //            throw new InvalidQNameListException(errors);
    //        }
    //    }
    //
    //}

}
