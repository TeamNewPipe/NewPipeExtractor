package org.schabi.newpipe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NewPipeTestRunnerOptions {
    /**
     * The ratio between the number of failed tests and the total number
     * of tests that have not been {@link org.junit.Ignore}d by the user.
     * Above this ratio the Runner will ignore all failing tests, since
     * there probably is a problem with YouTube blocking robots. A warning
     * along with a stack trace are displayed anyway.
     * @return the failedTests/notIgnoredTests ratio above which all failing tests are ignored
     */
    float failRatioTriggeringIgnore();
}
