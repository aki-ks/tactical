package me.aki.tactical.core.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Contains a value of either one or another type.
 *
 * One is referred as "left" the other as "right".
 *
 * @param <L> the "left" type
 * @param <R> the "right" type
 */
@SuppressWarnings("unchecked")
public class Either<L, R> {
    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(true, value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(false, value);
    }

    private final boolean isLeft;
    private final Object value;

    private Either(boolean isLeft, Object value) {
        this.isLeft = isLeft;
        this.value = value;
    }

    /**
     * Does this {@link Either} contain a left value
     *
     * @return is a left value present
     */
    public boolean isLeft() {
        return isLeft;
    }

    /**
     * Does this {@link Either} contain a right value
     *
     * @return is a right value present
     */
    public boolean isRight() {
        return !isLeft;
    }

    /**
     * Get the left value or throw an exception.
     *
     * @return the left value
     * @throws NoSuchElementException this {@link Either} contains a right value.
     */
    public L getLeft() {
        if (isLeft()) {
            return (L) value;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Get the right value or throw an exception.
     *
     * @return the right value
     * @throws NoSuchElementException this {@link Either} contains a left value.
     */
    public R getRight() {
        if (isRight()) {
            return (R) value;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Get the left value if this {@link Either} contains any.
     *
     * @return the left value
     */
    public Optional<L> getLeftOpt() {
        return isLeft() ? Optional.of((L) value) : Optional.empty();
    }

    /**
     * Get the right value if this {@link Either} contains any.
     *
     * @return the right value
     */
    public Optional<R> getRightOpt() {
        return isRight() ? Optional.of((R) value) : Optional.empty();
    }

    /**
     * Execute either an action for the left or the right value.
     *
     * @param ifLeft action to be executed if this {@link Either} contains a left
     * @param ifRight action to be executed if this {@link Either} contains a right
     */
    public void doEither(Consumer<L> ifLeft, Consumer<R> ifRight) {
        if (isLeft) {
            ifLeft.accept((L) value);
        } else {
            ifRight.accept((R) value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Either<?, ?> that = (Either<?, ?>) o;
        return this.isLeft == that.isLeft &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLeft, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(Either.class.getSimpleName());
        builder.append('{');
        builder.append(isLeft ? "left" : "right");
        builder.append('=');
        builder.append(value);
        builder.append('}');
        return builder.toString();
    }
}
