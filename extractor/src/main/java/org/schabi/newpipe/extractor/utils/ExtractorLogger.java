package org.schabi.newpipe.extractor.utils;

public final class ExtractorLogger {

    private ExtractorLogger() { }

    private static final Logger EMPTY_LOGGER = new EmptyLogger();
    private static volatile Logger logger = EMPTY_LOGGER;

    public static void setLogger(final Logger customLogger) {
        logger = customLogger != null ? customLogger : EMPTY_LOGGER;
    }

    public enum Level { DEBUG, WARN, ERROR }

    @SuppressWarnings("checkstyle:NeedBraces")
    private static void log(final Level level,
                            final String tag,
                            final String message,
                            final Throwable t) {
        if (logger == EMPTY_LOGGER) return;
        switch (level) {
            case DEBUG:
                if (t == null) {
                    logger.debug(tag, message);
                } else {
                    logger.debug(tag, message, t);
                }
                break;
            case WARN:
                if (t == null) {
                    logger.warn(tag, message);
                } else {
                    logger.warn(tag, message, t);
                }
                break;
            case ERROR:
                if (t == null) {
                    logger.error(tag, message);
                } else {
                    logger.error(tag, message, t);
                }
                break;
        }
    }

    @SuppressWarnings("checkstyle:NeedBraces")
    private static void logFormat(final Level level,
                                  final String tag,
                                  final Throwable t,
                                  final String template,
                                  final Object... args) {
        if (logger == EMPTY_LOGGER) return;
        log(level, tag, format(template, args), t);
    }

    // DEBUG
    public static void d(final String tag, final String msg) {
        log(Level.DEBUG, tag, msg, null);
    }

    public static void d(final String tag, final String msg, final Throwable t) {
        log(Level.DEBUG, tag, msg, t);
    }

    public static void d(final String tag, final String template, final Object... args) {
        logFormat(Level.DEBUG, tag, null, template, args);
    }

    public static void d(final String tag,
                         final Throwable t,
                         final String template,
                         final Object... args) {
        logFormat(Level.DEBUG, tag, t, template, args);
    }

    // WARN
    public static void w(final String tag, final String msg) {
        log(Level.WARN, tag, msg, null);
    }

    public static void w(final String tag, final String msg, final Throwable t) {
        log(Level.WARN, tag, msg, t);
    }

    public static void w(final String tag, final String template, final Object... args) {
        logFormat(Level.WARN, tag, null, template, args);
    }

    public static void w(final String tag,
                         final Throwable t,
                         final String template,
                         final Object... args) {
        logFormat(Level.WARN, tag, t, template, args);
    }

    // ERROR
    public static void e(final String tag, final String msg) {
        log(Level.ERROR, tag, msg, null);
    }

    public static void e(final String tag, final String msg, final Throwable t) {
        log(Level.ERROR, tag, msg, t);
    }

    public static void e(final String tag, final String template, final Object... args) {
        logFormat(Level.ERROR, tag, null, template, args);
    }

    public static void e(final String tag,
                         final Throwable t,
                         final String template,
                         final Object... args) {
        logFormat(Level.ERROR, tag, t, template, args);
    }

    /**
     * Simple string format method for easier logging in the form of
     * {@code ExtractorLogger.d("Hello my name {Name} {}", name, surname)}
     * @param template The template string to format
     * @param args Arguments to replace identifiers with in {@code template}
     * @return Formatted string with arguments replaced
     */
    private static String format(final String template, final Object... args) {
        if (template == null || args == null || args.length == 0) {
            return template;
        }
        final var out = new StringBuilder(template.length() + Math.min(32, 16 * args.length));
        int cursorIndex = 0;
        int argIndex = 0;
        final int n = template.length();
        while (cursorIndex < n) {
            // Find first/next open brace
            final int openBraceIndex = template.indexOf('{', cursorIndex);
            if (openBraceIndex < 0) {
                // If none found then there's no more arguments to replace
                out.append(template, cursorIndex, n); break;
            }

            // Find matching closing brace
            final int close = template.indexOf('}', openBraceIndex + 1);
            if (close < 0) {
                // If none found then there's no more arguments to replace
                out.append(template, cursorIndex, n); break;
            }
            // Append everything from cursor up to before the open brace
            out.append(template, cursorIndex, openBraceIndex);
            // Append arguments in the brace
            out.append(argIndex < args.length
                ? String.valueOf(args[argIndex++])
                : template.substring(openBraceIndex, close + 1));
            cursorIndex = close + 1;
        }
        return out.toString();
    }

    private static final class EmptyLogger implements Logger {
        public void debug(final String tag, final String msg) { }
        public void debug(final String tag, final String msg, final Throwable throwable) { }
        public void warn(final String tag, final String msg) { }
        public void warn(final String tag, final String msg, final Throwable t) { }
        public void error(final String tag, final String msg) { }
        public void error(final String tag, final String msg, final Throwable t) { }
    }

    public static final class ConsoleLogger implements Logger {
        public void debug(final String tag, final String msg) {
            System.out.println("[DEBUG][" + tag + "] " + msg);
        }

        public void debug(final String tag, final String msg, final Throwable throwable) {
            debug(tag, msg);
            throwable.printStackTrace(System.err);
        }
        public void warn(final String tag, final String msg) {
            System.out.println("[WARN ][" + tag + "] " + msg);
        }

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
