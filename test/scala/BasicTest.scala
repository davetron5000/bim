package bim

import org.junit._
import org.junit.Assert._

class BasicTest {
  @Test
  def testThatPasses = {
    assertEquals(10,10)
  }

  @Test
  def testThatFails = {
    assertEquals(9,9)
  }
}
// vim: set ts=2 sw=2 et:
