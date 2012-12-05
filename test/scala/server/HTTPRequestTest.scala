package bim.server

import org.junit._
import org.junit.Assert._

class HTTPRequestTest {
  @Test
  def `test header method` = {
    val request = HTTPRequest(method  = "GET",
                               uri     = "/",
                               version = "1.1",
                               headers = Map("oneValue"  -> List("bar"),
                                             "twoValues" -> List("blah","crud")))

    assertEquals("bar",request.header("oneValue").get)
    assertEquals("blah",request.header("twoValues").get)
    assertEquals(None,request.header("whatever"))
  }
}
