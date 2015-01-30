package ch.raffael.sangria.assembly;


import java.util.ArrayList;
import java.util.List;

import ch.raffael.sangria.libs.guava.base.Joiner;
import ch.raffael.sangria.libs.guava.base.Predicate;
import ch.raffael.sangria.libs.guava.base.Throwables;
import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.collect.Iterables;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class MessageCollector extends AbstractMessager {

    private static final Joiner JOINER = Joiner.on(System.lineSeparator());
    private static final Predicate<Message> ERRORS_ONLY = new Predicate<Message>() {
        @Override
        public boolean apply(Message input) {
            return input.type.ordinal() >= MessageType.ERROR.ordinal();
        }
    };

    private final List<Message> messages = new ArrayList<>();

    @Override
    public void error(Source source, String message, Throwable cause) {
        messages.add(new Message(MessageType.ERROR, source, message, cause));
    }

    @Override
    public void warning(Source source, String message, Throwable cause) {
        messages.add(new Message(MessageType.WARNING, source, message, cause));
    }

    public List<Message> getMessages() {
        return ImmutableList.copyOf(messages);
    }

    public String getMessagesAsString() {
        return JOINER.join(messages);
    }

    public String getErrorsAsString() {
        return JOINER.join(Iterables.filter(messages, ERRORS_ONLY));
    }

    public static final class Message {

        private final MessageType type;
        private final Object source;
        private final String message;
        private final Throwable cause;

        private Message(MessageType type, Object source, String message, Throwable cause) {
            this.type = type;
            this.source = source;
            this.message = message;
            this.cause = cause;
        }

        @Override
        public String toString() {
            return type + ": " + (source == null ? "(unknown source)" : source)
                    + ": " + message
                    + (cause == null ? "" : System.lineSeparator() + Throwables.getStackTraceAsString(cause));
        }

    }

    public static enum MessageType {
        WARNING, ERROR
    }

}
