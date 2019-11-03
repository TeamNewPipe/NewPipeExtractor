package org.schabi.newpipe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NewPipeTestRunnerOptions {
    /**
     * This tells the Runner to wait some time before running
     * the tests in the annotated class.
     * @return milliseconds to sleep before testing the class
     */
    int classDelayMs() default 0;

    /**
     * This tells the Runner to wait some time before invoking
     * each test method in the annotated class.
     * @return milliseconds to sleep before invoking each test method
     */
    int methodDelayMs() default 0;
}
