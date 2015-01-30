package ch.raffael.sangria.assembly;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractMessager implements Messager {

    @Override
    public Messager substituteSource(final Source newSource) {
        return new AbstractMessager() {
            @Override
            public void error(Source source, String message, Throwable cause) {
                AbstractMessager.this.error(newSource, message, cause);
            }
            @Override
            public void warning(Source source, String message, Throwable cause) {
                AbstractMessager.this.warning(newSource, message, cause);
            }
        };
    }

    @Override
    public Messager formatMessage(final String format) {
        return new AbstractMessager() {
            @Override
            public void error(Source source, String message, Throwable cause) {
                AbstractMessager.this.error(source, String.format(format, message), cause);
            }
            @Override
            public void warning(Source source, String message, Throwable cause) {
                AbstractMessager.this.warning(source, String.format(format, message), cause);
            }
        };
    }

    @Override
    public void error(Source source, String message) {
        error(source, message, null);
    }

    @Override
    public void warning(Source source, String message) {
        error(source, message, null);
    }

}
