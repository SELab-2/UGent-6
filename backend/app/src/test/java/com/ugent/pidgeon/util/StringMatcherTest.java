package com.ugent.pidgeon.util;

import org.junit.jupiter.api.Test;

public class StringMatcherTest {

  @Test
  public void testIsValidEmail() {
    assert (StringMatcher.isValidEmail("name.surname@UGent.be"));
    assert (StringMatcher.isValidEmail("namesurname@UGent.be"));

  }

  @Test
  public void testIsValidEmailNoEndPart() {
    assert (!StringMatcher.isValidEmail("name.surname@UGent"));
  }

  @Test
  public void testIsValidEmailNoAt() {
    assert (!StringMatcher.isValidEmail("name.surnameUGent.be"));
  }

  @Test
  public void testIsValidEmailNoStartPart() {
    assert (!StringMatcher.isValidEmail("@UGent.be"));
  }

  @Test
  public void testIsValidEmailNoDot() {
    assert (!StringMatcher.isValidEmail("name.surname@UGentbe"));

  }







}
