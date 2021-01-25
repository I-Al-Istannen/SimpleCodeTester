package me.ialistannen.simplecodetester.backend.util;

/**
 * Represents an operation that accepts a single input argument, returns no result but could
 * possibly throw an exception.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of the exception
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Throwable> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 * @throws E an exception if any occurs during the execution of the operation
	 */
	void accept(T t) throws E;

}
