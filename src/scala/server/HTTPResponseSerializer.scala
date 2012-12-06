package bim
package server

import scala.language.implicitConversions

import java.io.OutputStream

/** Serializes a response to a stream */
object HTTPResponseSerializer {
  implicit def stringToBytes(s:String):Array[Byte] = s.getBytes("utf-8")

  def serialize(response:HTTPResponse, outputStream:OutputStream): Unit = {
    outputStream.write(s"HTTP/${response.version} ${response.status} ${response.reasonPhrase}\r\n")
    outputStream.write(response.headers.map { case (header,values) =>
      values.map { value => s"$header: $value\r\n" }
    }.flatten.mkString)
    outputStream.write("\r\n")
    response.body.foreach { outputStream.write(_) }
  }
}
