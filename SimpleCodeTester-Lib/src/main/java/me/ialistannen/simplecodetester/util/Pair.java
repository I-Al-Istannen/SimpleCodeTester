package me.ialistannen.simplecodetester.util;

import java.util.Map.Entry;
import java.util.Objects;

/**
 * Represents a simple key-value pair.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class Pair<K, V> {

  private final K key;
  private final V value;

  /**
   * Creates a new Pair with the given key and value.
   *
   * @param key the key
   * @param value the value
   */
  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Creates a new Pair with the key and value of the given map entry.
   *
   * @param entry the {@link Entry} to get the information from
   */
  public Pair(Entry<K, V> entry) {
    this(entry.getKey(), entry.getValue());
  }

  /**
   * Returns the key.
   *
   * @return the key
   */
  public K getKey() {
    return key;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  public V getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(key, pair.key) &&
        Objects.equals(value, pair.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "Pair{" +
        "key=" + key +
        ", value=" + value +
        '}';
  }
}
