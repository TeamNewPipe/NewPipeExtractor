package org.schabi.newpipe.extractor.utils;

public final class ExtractorLogger {

    private ExtractorLogger() { }

    private static Logger logger = new EmptyLogger();

    public static void setLogger(final Logger customLogger) {
        logger = customLogger;
    }

    public static void d(final String tag, final String msg) {
        logger.debug(tag, msg);
    }

    public static void d(final String tag, final String msg, final Throwable t) {
        logger.debug(tag, msg, t);
    }

    public static void w(final String tag, final String msg) {
        logger.warn(tag, msg);
    }

    public static void w(final String tag, final String msg, final Throwable t) {
        logger.warn(tag, msg, t);
    }

    public static void e(final String tag, final String msg) {
        logger.error(tag, msg);
    }

    public static void e(final String tag, final String msg, final Throwable t) {
        logger.error(tag, msg, t);
    }


    private static final class EmptyLogger implements Logger {
        public void debug(final String tag, final String msg) { }

        @Override
        public void debug(final String tag, final String msg, final Throwable throwable) { }

        public void warn(final String tag, final String msg) { }

        @Override
        public void warn(final String tag, final String msg, final Throwable t) { }

        public void error(final String tag, final String msg) { }

        public void error(final String tag, final String msg, final Throwable t) { }
    }

    /**
     *  Logger that prints to stdout
     */
    public static final class ConsoleLogger implements Logger {
        public void debug(final String tag, final String msg) {
        System.out.println("[DEBUG][" + tag + "] " + msg);
        }

        @Override
        public void debug(final String tag, final String msg, final Throwable throwable) {
            debug(tag, msg);
            throwable.printStackTrace(System.err);
        }

        public void warn(final String tag, final String msg) {
            System.out.println("[WARN ][" + tag + "] " + msg);
        }

        @Override
        public void warn(final String tag, final String msg, final Throwable t) {
            warn(tag, msg);
            t.printStackTrace(System.err);
        }

        public void error(final String tag, final String msg) {
            System.err.println("[ERROR][" + tag + "] " + msg);
        }

        public void error(final String tag, final String msg, final Throwable t) {
            System.err.println("[ERROR][" + tag + "] " + msg);
            t.printStackTrace(System.err);
        }
    }
}
