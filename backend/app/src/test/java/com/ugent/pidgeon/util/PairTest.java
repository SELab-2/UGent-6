package com.ugent.pidgeon.util;

import org.junit.jupiter.api.Test;

public class PairTest {

  @Test
  public void testPair() {
    Pair<String, Integer> pair = new Pair<>("test", 1);
    assert(pair.getFirst().equals("test"));
    assert(pair.getSecond().equals(1));
  }

}
