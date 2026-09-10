package org.schabi.newpipe.extractor.utils;

import javax.annotation.Nonnull;

/**
 * Interface to execute JavaScript code.
 *
 * <p>
 * Some clients, such as the WEB player, require to execute JavaScript code to allow playback.
 * </p>
 */
public interface JavaScriptInterpreter {

    /**
     * Runs the specified function in the code.
     *
     * @param code the code that contains the function that should be run
     * @param functionName the name of the function that should be run
     * @param parameters the parameters to run with the function
     * @return the result of the run function
     */
    String run(@Nonnull final String code,
               @Nonnull final String functionName,
               final String... parameters);
}

