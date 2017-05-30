package com.episode6.hackit.disposable;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SomeClassTest {

  @Test
  public void placeholderTest() {
    int input = 1;
    int expectedOutput = 2;

    int output = SomeClass.increment(input);

    assertThat(output).isEqualTo(expectedOutput);
  }
}
