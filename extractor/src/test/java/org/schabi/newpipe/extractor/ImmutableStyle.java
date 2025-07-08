package org.schabi.newpipe.extractor;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// CHECKSTYLE:OFF
/**
 * Custom style for generated Immutables.
 * See <a href="https://immutables.github.io/style.html">Style</a>.
 * <p>
 * - Abstract types start with 'I' (e.g., IExample).<p>
 * - Concrete immutable types do not have a prefix (e.g., Example).<p>
 * - Getters are prefixed with 'get', 'is', or no prefix.<p>
 * - <a href="https://immutables.github.io/immutable.html#strict-builder">Strict builder pattern is enforced.</a><p>
 */
// CHECKSTYLE:ON
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Value.Style(
    get = {"get*", "is*", "*"}, // Methods matching these prefixes will be used as getters.
                                // Methods matching these patterns can NOT be used as setters.
    typeAbstract = {"I*"}, // Abstract types start with I
    typeImmutable = "*", // Generated concrete Immutable types will not have the I prefix
    visibility = Value.Style.ImplementationVisibility.PUBLIC,
    strictBuilder = true,
    defaultAsDefault = true, // https://immutables.github.io/immutable.html#default-attributes
    jdkOnly = true
)
public @interface ImmutableStyle { }
