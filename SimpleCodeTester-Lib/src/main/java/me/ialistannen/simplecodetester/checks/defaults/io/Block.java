package me.ialistannen.simplecodetester.checks.defaults.io;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * A grouped collection of some type that allows iteration.
 *
 * @param <T> th type of the collection
 */
public class Block<T> {

  private final List<T> data;
  private transient int position;
  private transient Queue<Integer> markedPositions;

  /**
   * Creates a new Block with the given data.
   *
   * @param data the data
   */
  public Block(List<T> data) {
    this.data = data;
    this.markedPositions = new ArrayDeque<>();
  }

  /**
   * Returns whether there is any data left in the block.
   *
   * @return true if there is any data left in the block
   */
  public boolean hasNext() {
    return position < data.size();
  }

  /**
   * Returns the next element and advances this block.
   *
   * @return the next element
   * @throws IndexOutOfBoundsException if {@link #hasNext()} is not true
   */
  public T next() {
    T output = data.get(position);

    if (hasNext()) {
      position++;
    }
    return output;
  }

  /**
   * Returns whether this block is at the start of its elements.
   *
   * @return true if this block is at the start of its elements
   */
  boolean isAtStart() {
    return position == 0;
  }

  /**
   * Creates a shallow copy of this block that will start iterating at the first element again.
   *
   * @return a shallow copy
   */
  Block<T> copyFromStart() {
    return new Block<>(data);
  }

  /**
   * Marks the position so later calls to
   */
  void mark() {
    markedPositions.offer(position);
  }

  /**
   * Returns all data since the last mark, removing the mark in the process.
   *
   * @return all data since the last mark
   */
  List<T> getFromLastMark() {
    int oldPosition = markedPositions.remove();

    return data.subList(oldPosition, position);
  }

  @Override
  public String toString() {
    return "Block{" +
        "data=" + data +
        ", position=" + position +
        '}';
  }
}