package me.ialistannen.simplecodetester.backend.util;

/**
 * Represents a function that accepts a single input argument, returns a result and could possibly
 * throw an exception.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the function's return value
 * @param <E> the type of the exception
 */
@FunctionalInterface
public interface CheckedFunction<T, R, E extends Throwable> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 * @return R on successful evaulation
	 * @throws E an exception if any occurs during the execution of the operation
	 */
	R accept(T t) throws E;

}
