package me.ialistannen.simplecodetester.checks.defaults.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlockTest {

  private Block<String> block;

  @BeforeEach
  void seup() {
    block = new Block<>(List.of("Hello", "world", "!"));
  }

  @Test
  void nextAndHasNext() {
    assertTrue(block.hasNext());
    assertEquals(block.next(), "Hello");

    assertTrue(block.hasNext());
    assertEquals(block.next(), "world");

    assertTrue(block.hasNext());
    assertEquals(block.next(), "!");

    assertFalse(block.hasNext());
  }

  @Test
  void callingNextInInvalidStateThrowsException() {
    Block<String> block = new Block<>(Collections.emptyList());

    assertThrows(
        IndexOutOfBoundsException.class,
        block::next
    );
  }

  @Test
  void copyDoesNotStartFromOldCursorPosition() {
    block.next();
    block.next();

    assertEquals(
        block.copyFromStart().next(),
        "Hello"
    );
  }

  @Test
  void isAtStartWhenNotAtStart() {
    block.next();

    assertFalse(block.isAtStart());
  }

  @Test
  void isAtStartWhenAtStart() {
    assertTrue(block.isAtStart());
  }

  @Test
  void copyIsAtStart() {
    block.next();
    assertTrue(block.copyFromStart().isAtStart());
  }

}