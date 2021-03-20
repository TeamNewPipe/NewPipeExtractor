package org.schabi.newpipe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

/**
 * Marker annotation to skip test in certain cases.
 *
 * {@link MockOnlyRule}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface MockOnly {

    /**
     * Explanation why this test should be skipped
     */
    @Nonnull String reason();
}