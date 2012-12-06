package bim
package server

import java.net._

object InlineDispatcher extends Dispatcher {
  def dispatch(socket:Socket) = try {

    val inputStream  = socket.getInputStream
    val outputStream = socket.getOutputStream
    val request      = HTTPRequestParser.parse(inputStream)
    val response     = EverythingsOKHandler.handle(request)

    HTTPResponseSerializer.serialize(response,outputStream)

    outputStream.flush
    outputStream.close
  }
  catch {
    case ex:SocketTimeoutException => {
      println(s"Client socket exception: ${ex.getMessage}")
      socket.close
    }
  }
}
