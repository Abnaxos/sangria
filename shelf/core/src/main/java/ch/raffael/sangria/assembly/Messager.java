package ch.raffael.sangria.assembly;

import ch.raffael.sangria.libs.guava.base.Predicate;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Messager {

    void error(Source source, String message);

    void error(Source source, String message, Throwable cause);

    void warning(Source source, String message);

    void warning(Source source, String message, Throwable cause);

    Messager substituteSource(Source source);

    Messager formatMessage(String format);

    public static enum Severity {
        INFO, WARNING, ERROR;
        private final Predicate<Message> equals = new Predicate<Message>() {
            @Override
            public boolean apply(Message input) {
                return input != null && MessageCollector.Severity.this.equals(input.getSeverity());
            }
        };
        private final Predicate<Message> moreSevere = new Predicate<Message>() {
            @Override
            public boolean apply(Message input) {
                return input != null && MessageCollector.Severity.this.equals(input.getSeverity());
            }
        };

    }

    public static final class Message {

        private final Severity severity;
        private final String message;
        private final Source source;

        public Message(Severity severity, Source source, String message) {
            this.severity = severity;
            this.message = message;
            this.source = source;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }

        public Object getSource() {
            return source;
        }

    }

    interface Source {
    }
}
