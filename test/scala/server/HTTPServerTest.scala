package bim.server

import org.junit._
import org.junit.Assert._

import scala.util.Random
import java.net._
import java.io.InputStream

class HTTPServerTest {

  class ServerRunner(server:HTTPServer) extends Runnable {
    def run:Unit = server.start
  }

  private var localhost : InetAddress = _
  private var server    : HTTPServer  = _
  private var thread    : Thread      = _
  private var socket    : Socket      = _

  @Before
  def `startup server` = {
    localhost = InetAddress.getLocalHost
    server    = new HTTPServer(8080,localhost)
    thread    = new Thread(new ServerRunner(server))

    thread.start
  }

  @After
  def `shutdown server` = {
    socket.close
    server.stop
    thread.join
  }

  @Test
  def `basic test` = {
    socket = new Socket(localhost.getHostAddress,8080)
    socket.getOutputStream.write("GET /foo/bar HTTP/1.1\r\n\r\n".getBytes("utf-8"))

    assertEquals("HTTP/1.1 200 OK\r\n\r\n",slurpStream(socket.getInputStream))
  }

  @Test
  def `legit bad request` = {
    socket = new Socket(localhost.getHostAddress,8080)
    socket.getOutputStream.write("GET /foo/bar HTTP/1.1\r\nblah\r\n".getBytes("utf-8"))

    assertEquals("HTTP/1.1 400 Bad Request\r\n\r\n",slurpStream(socket.getInputStream))
  }

  @Test
  def `nonsense request` = {
    socket = new Socket(localhost.getHostAddress,8080)
    socket.getOutputStream.write("asdfljahsdlfkjhasldfkjhasldfjhasldf".getBytes("utf-8"))

    assertEquals("HTTP/1.1 400 Bad Request\r\n\r\n",slurpStream(socket.getInputStream))
  }

  @Test
  def `timeout once connected` = {
    socket = new Socket(localhost.getHostAddress,8080)
    socket.getOutputStream.write("GET ".getBytes("utf-8"))
    Thread.sleep(100)

    assert(socket.isConnected,"Expected it to be disconnected due to timeout")
  }

  private def slurpStream(stream:InputStream):String = {
    val buffer = new StringBuffer("")
    var ch = stream.read
    while (ch != -1) {
      buffer.append(ch.toChar)
      ch = stream.read
    }
    buffer.toString
  }
}
// vim: set ts=2 sw=2 et:
