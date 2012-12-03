package bim.server

import org.junit._
import org.junit.Assert._

import scala.util.Random
import java.io._

class HTTPRequestParserTest {
  val testDataLocation = "test/data/http_requests"

  @Test
  def `minimal request` = {
    val request = "GET / HTTP/1.1\r\n\r\n"
    val inputStream = new ByteArrayInputStream(request.getBytes("utf-8"))

    val parsed = HTTPRequestParser.parse(inputStream)

    parsed.fold(
      error => fail(s"Expected a successful parse ($error)"),
      parsedRequest => {
        assertEquals("GET",parsedRequest.method)
        assertEquals("/",parsedRequest.uri)
        assertEquals("1.1",parsedRequest.version)
        assert(parsedRequest.headers.isEmpty,parsedRequest.headers.toString)
        assertEquals(None,parsedRequest.body)
      }
    )
  }

  @Test
  def `request with headers` = {
    val accept = "text/html"
    val userAgent = "foobar"
    val request = List(
      "GET / HTTP/1.1",
      s"Accept: $accept",
      s"User-Agent: $userAgent",
      ""
      ).mkString("\r\n")
    val inputStream = new ByteArrayInputStream(request.getBytes("utf-8"))

    val parsed = HTTPRequestParser.parse(inputStream)

    parsed.fold(
      error => fail(s"Expected a successful parse :$error"),
      parsedRequest => {
        assertEquals("GET",parsedRequest.method)
        assertEquals("/",parsedRequest.uri)
        assertEquals("1.1",parsedRequest.version)
        assertEquals(accept,parsedRequest.headers("accept"))
        assertEquals(userAgent,parsedRequest.headers("user-agent"))
        assertEquals(2,parsedRequest.headers.size)
        assertEquals(None,parsedRequest.body)
      }
    )
  }

  val BAD_REQUESTS = List(
    "GET / HTTP/1.1\r\nasdfaskdfjasldfkjasd\r\n",
    "GET / HTTP/1.1\r\nAccept: text/html\r\nasdfaskdfjasldfkjasd\r\n",
    "GET / HTP/1.1\r\n",
    "GET / TTPH/1.1\r\n",
    "GET / HTTP/111\r\n",
    "GET / HTTP/1.1\r",
    "GET / HTTP/1.1",
    "GET / HTTP/1",
    "GET /",
    "GET ",
    "G",
    ""
  )

  @Test
  def `minimal bad requests` = {
    BAD_REQUESTS.foreach { request =>
      val inputStream = new ByteArrayInputStream(request.getBytes("utf-8"))

      val parsed = HTTPRequestParser.parse(inputStream)

      parsed.fold(
        error => (),
        parsedRequest => fail(s"Expected a bad request for $request")
      )
    }
  }
}
