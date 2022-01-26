package org.schabi.newpipe.extractor.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Serializable class to create a pair of objects.
 *
 * <p>
 * The two objects of the pair must be {@link Serializable serializable} and can be of the same
 * type.
 * </p>
 *
 * <p>
 * Note that this class is not intended to be used as a general-purpose pair and should only be
 * used when interfacing with the extractor.
 * </p>
 *
 * @param <F> the type of the first object, which must be {@link Serializable}
 * @param <S> the type of the second object, which must be {@link Serializable}
 */
public class Pair<F extends Serializable, S extends Serializable> implements Serializable {

    private static final long serialVersionUID = -6526893387870149407L;

    /**
     * The first object of the pair.
     */
    private F firstObject;

    /**
     * The second object of the pair.
     */
    private S secondObject;

    /**
     * Creates a new {@link Pair} object.
     *
     * @param firstObject  the first object of the pair
     * @param secondObject the second object of the pair
     */
    public Pair(final F firstObject, final S secondObject) {
        this.firstObject = firstObject;
        this.secondObject = secondObject;
    }

    /**
     * Sets the first object, which must be of the {@link F} type.
     *
     * @param firstObject the new first object of the pair
     */
    public void setFirst(final F firstObject) {
        this.firstObject = firstObject;
    }

    /**
     * Sets the first object, which must be of the {@link S} type.
     *
     * @param secondObject the new first object of the pair
     */
    public void setSecond(final S secondObject) {
        this.secondObject = secondObject;
    }

    /**
     * Gets the first object of the pair.
     *
     * @return the first object of the pair
     */
    public F getFirst() {
        return firstObject;
    }

    /**
     * Gets the second object of the pair.
     *
     * @return the second object of the pair
     */
    public S getSecond() {
        return this.secondObject;
    }

    /**
     * Returns a string representation of the current {@code Pair}.
     *
     * <p>
     * The string representation will look like this:
     * <code>
     * {<i>firstObject.toString()</i>, <i>secondObject.toString()</i>}
     * </code>
     * </p>
     *
     * @return a string representation of the current {@code Pair}
     */
    @Override
    public String toString() {
        return "{" + firstObject.toString() + ", " + secondObject.toString() + "}";
    }

    /**
     * Reveals whether an object is equal to this {@code Pair} instance.
     *
     * @param obj the object to compare with this {@code Pair} instance
     * @return whether an object is equal to this {@code Pair} instance
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Pair<?, ?> pair = (Pair<?, ?>) obj;
        return Objects.equals(firstObject, pair.firstObject) && Objects.equals(secondObject,
                pair.secondObject);
    }

    /**
     * Returns a hash code of the current {@code Pair} by using the first and second object.
     *
     * @return a hash code of the current {@code Pair}
     */
    @Override
    public int hashCode() {
        return Objects.hash(firstObject, secondObject);
    }
}
