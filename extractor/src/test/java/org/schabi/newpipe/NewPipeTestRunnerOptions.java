package org.schabi.newpipe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NewPipeTestRunnerOptions {
    /**
     * This tells the Runner to wait the specified time before
     * running the tests in the annotated class.
     * @return milliseconds to sleep before testing the class
     */
    int classDelayMs() default 0;

    /**
     * This tells the Runner to wait the specified time before
     * invoking each test method in the annotated class.
     * @return milliseconds to sleep before invoking each test method
     */
    int methodDelayMs() default 0;

    /**
     * This tells the Runner to retry at most the specified number of
     * times running the tests in the annotated class. As soon as the
     * maximum number of retries is hit or all test methods succeed,
     * the Runner completes, respectively with the last errors or
     * with success.
     * @return the number of times the class should be tested before
     * declaring one of its test methods as failed.
     */
    int retry() default 1;
}
