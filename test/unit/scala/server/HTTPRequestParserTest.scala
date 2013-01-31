package bim.server

import org.junit._
import org.junit.Assert._

import scala.util.Random
import java.io._
import java.net._

import org.mockito.Mockito._

class HTTPRequestParserTest {
  val testDataLocation = "test/data/http_requests"

  @Test
  def `request with a timeout` = {
    val inputStream = mock(classOf[InputStream])
    val exceptionMessage = "There was a timeout"
    when(inputStream.read).thenThrow(new SocketTimeoutException(exceptionMessage))
    val parsed = HTTPRequestParser.parse(inputStream)
    parsed.fold(
      error => {
        assertEquals(exceptionMessage,error.errorMessage)
        assertEquals(classOf[SocketTimeoutException],error.exception.get.getClass)
      },
      parsedRequest => fail("Expected an error, but got a " + parsedRequest)
    )
  }

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
  def `very minimal request` = {
    val request = "GET /\r\n\r\n"
    val inputStream = new ByteArrayInputStream(request.getBytes("utf-8"))

    val parsed = HTTPRequestParser.parse(inputStream)

    parsed.fold(
      error => fail(s"Expected a successful parse ($error)"),
      parsedRequest => {
        assertEquals("GET",parsedRequest.method)
        assertEquals("/",parsedRequest.uri)
        assertEquals("1.0",parsedRequest.version)
        assert(parsedRequest.headers.isEmpty,parsedRequest.headers.toString)
        assertEquals(None,parsedRequest.body)
      }
    )
  }

  @Test
  def `minimal request ignores body` = {
    val request = "GET / HTTP/1.1\r\n\r\nasdkfhaskdjfhaskdfhaskdfhkashfjsdkf"
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
    val accept1 = "text/html"
    val accept2 = "text/xml"
    val accept3 = "text/plain"
    val userAgent = "foobar"
    val request = List(
      "GET / HTTP/1.1",
      s"Accept: $accept1",
      s"User-Agent: $userAgent",
      s"Accept: $accept2, $accept3",
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
        assert(parsedRequest.headers("accept").contains(accept1))
        assert(parsedRequest.headers("accept").contains(accept2))
        assert(parsedRequest.headers("accept").contains(accept3))
        assertEquals(3,parsedRequest.headers("accept").size)
        assertEquals(userAgent,parsedRequest.header("user-agent").get)
        assertEquals(2,parsedRequest.headers.size)
        assertEquals(None,parsedRequest.body)
      }
    )
  }

  @Test
  def `request with headers and a body` = {
    val userAgent = "foobar"
    val content = "This is the content that we are\npassing along\nand it's got stuff in it\r\nAnd all that"
    val request = List(
      "GET / HTTP/1.1",
      "Content-Length: " + content.length,
      s"User-Agent: $userAgent",
      "",
      content
      ).mkString("\r\n")
    val inputStream = new ByteArrayInputStream(request.getBytes("utf-8"))

    val parsed = HTTPRequestParser.parse(inputStream)

    parsed.fold(
      error => fail(s"Expected a successful parse :$error"),
      parsedRequest => {
        assertEquals("GET",parsedRequest.method)
        assertEquals("/",parsedRequest.uri)
        assertEquals("1.1",parsedRequest.version)
        assertEquals(userAgent,parsedRequest.header("user-agent").get)
        assertEquals(2,parsedRequest.headers.size)
        parsedRequest.body match {
          case None => fail("Expected a body")
          case Some(body) => assertEquals(content,new String(body,"utf-8"))
        }
      }
    )
  }

  val BAD_REQUESTS = List(
    "GET / HTTP/1.1\r\nContent-Length: 5000\r\nadfasdfasdf",
    "GET / HTTP/1.1\r\nAccept: text/html\r\nasdfaskdfjasldfkjasd\r\n",
    "GET / HTP/1.1\r\n",
    "GET / TTPH/1.1\r\n",
    "GET / HTTP/111\r\n",
    "GET / HTTP/1.1\r",
    "GET / HTTP/1.1",
    "GET / HTTP/1",
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
