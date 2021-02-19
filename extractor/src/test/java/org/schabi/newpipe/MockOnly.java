package org.schabi.newpipe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

/**
 * Marker annotation to skip test if it not run with mocks.
 *
 * {@link MockOnlyRule}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface MockOnly {

    /**
     * Explanation why this test shold only be run with mocks and not against real websites
     */
    @Nonnull String reason();
}