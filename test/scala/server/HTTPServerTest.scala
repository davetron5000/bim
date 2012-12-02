package bim.server

import org.junit._
import org.junit.Assert._
import org.mockito.Mockito._

import scala.util.Random
import java.net._

class HTTPServerTest {

  @Test
  def `server, when started, will listen on the configured address and port`:Unit = {
    val serverSocket                   = mock(classOf[ServerSocket])
    var portGiven        : Int         = -1
    var inetAddressGiven : InetAddress = null
    val port                           = anyPort
    val inetAddress                    = anyInetAddress

    val server  = HTTPServer(port,inetAddress,{ (p:Int, i:InetAddress) => 
      portGiven        = port
      inetAddressGiven = inetAddress
      serverSocket 
    })

    when(serverSocket.accept()).thenReturn(null)

    server.start

    verify(serverSocket).accept()
    assertEquals(port,portGiven)
    assertEquals(inetAddress,inetAddressGiven)
  }

  private def anyPort = Random.nextInt(65536)
  private def anyInetAddress = {
    val one   = Random.nextInt(256)
    val two   = Random.nextInt(256)
    val three = Random.nextInt(256)
    val four  = Random.nextInt(256)

    InetAddress.getByName(s"$one.$two.$three.$four")
  }
}
// vim: set ts=2 sw=2 et:
