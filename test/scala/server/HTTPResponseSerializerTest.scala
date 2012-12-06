package bim.server

import org.junit._
import org.junit.Assert._

import scala.util.Random
import java.io._

class HTTPResponseSerializerTest {

  @Test
  def `test a simple status with no headers or body` = {
    val response = HTTPResponse(status = "200",
                                reasonPhrase = "OK")

    val outputStream = new ByteArrayOutputStream()

    HTTPResponseSerializer.serialize(response,outputStream)

    assertEquals("HTTP/1.1 200 OK\r\n\r\n",new String(outputStream.toByteArray,"utf-8"))
  }

  @Test
  def `test a status with headers but no body` = {
    val response = HTTPResponse(status = "200",
                                reasonPhrase = "OK",
                                headers = Map("foo" -> List("bar"),
                                              "blah" -> List("one","two")))

    val outputStream = new ByteArrayOutputStream()

    HTTPResponseSerializer.serialize(response,outputStream)

    // We don't want to sort the keys just to get deterministic output
    val possible1 = "HTTP/1.1 200 OK\r\nfoo: bar\r\nblah: one\r\nblah: two\r\n\r\n"
    val possible2 = "HTTP/1.1 200 OK\r\nblah: one\r\nblah: two\r\nfoo: bar\r\n\r\n"
    val result = new String(outputStream.toByteArray,"utf-8")

    if ((result != possible1) && (result != possible2)) {
      assertEquals(possible1,result) // just to give a good error message
    }
  }

  @Test
  def `test a status with headers and a body` = {
    val body = "This is the body and all that that entails"
    val response = HTTPResponse(status = "200",
                                reasonPhrase = "OK",
                                headers = Map("foo" -> List("bar")),
                                body = Some(body.getBytes("utf-8")))

    val outputStream = new ByteArrayOutputStream()

    HTTPResponseSerializer.serialize(response,outputStream)

    assertEquals(s"HTTP/1.1 200 OK\r\nfoo: bar\r\n\r\n$body",new String(outputStream.toByteArray,"utf-8"))

  }
}
