package bim
package server

import java.io.InputStream

object HTTPRequestParser {
  class ParseError extends RuntimeException

  /**
   * parse the given InputStream as an HTTP request.
   *
   * @param inputStream input stream containing bytes that might be an HTTP request
   *
   * @return If a `Right`, the request was parsed successfully.  If a `Left`, there was a problem
   */
  def parse(inputStream:InputStream): Either[HTTPRequestParseError,HTTPRequest] = {
    for (method  <- readUntil(inputStream,SPACE).right;
         uri     <- readUntil(inputStream,SPACE).right;
         version <- parseVersion(inputStream).right;
         headers <- parseHeaders(inputStream).right;
         body    <- parseBody(inputStream,headers).right) yield HTTPRequest(method,uri,version,headers,body)
  }

  private val SPACE : Int = ' '.toInt
  private val LF    : Int = '\n'.toInt
  private val CR    : Int = '\r'.toInt
  private val COLON : Int = ':'. toInt

  private def readUntil(inputStream:InputStream, stopChar:Int):Either[HTTPRequestParseError,String] = {
    val buffer = new StringBuffer(256)
    var ch = inputStream.read()
    while (ch != stopChar) {
      if (ch == -1) {
        return Left(HTTPRequestParseError("EOF while reading method or URI"))
      }
      buffer.append(ch.toChar)
      ch = inputStream.read()
    }
    Right(buffer.toString)
  }

  private def parseVersion(inputStream:InputStream):Either[HTTPRequestParseError,String] = {
    readUntil(inputStream,LF) match {
      case Left(v)  => Left(v)
      case Right(v) if v.startsWith("HTTP/") && v.endsWith("\r") && v.indexOf(".") >= 6 => Right(v.substring(5,v.length() - 1))
      case Right(v) => { Left(HTTPRequestParseError("'" + v.trim + "' doesn't look like an HTTP version string")) }
    }
  }

  private def parseHeaders(inputStream:InputStream):Either[HTTPRequestParseError,Map[String,List[String]]] = {
    var done    : Boolean                  = false
    var headers : Map[String,List[String]] = Map()
    def isCRorLF(ch:Int)                   = (ch == CR) || (ch == LF)
    var ch                                 = inputStream.read()

    while (!done) {
      var key               = new StringBuffer(80)
      var value             = new StringBuffer(256)
      var inValue : Boolean = false
      var prevCh            = -1

      while ( (prevCh != CR) && (ch != LF) ) {

        if (ch == -1) return Left(HTTPRequestParseError("EOF while parsing headers"))

        if (inValue) {
          unless (isCRorLF(ch)) { 
            value.append(ch.toChar)
          }
        }
        else if (ch == COLON) {
          inValue = true
        }
        else {
          unless (isCRorLF(ch)) {
            key.append(ch.toChar)
          }
        }

        prevCh = ch
        ch     = inputStream.read()
      }
      if (key.length() > 0) {
        if (!inValue) return Left(HTTPRequestParseError(s"Expected : for header $key"))
        val header = key.toString.toLowerCase.trim
        val newList = (headers.get(header) match {
          case Some(list) => list
          case None => List[String]()
        }) ++ value.toString.split(",").map { _.trim }

        headers = headers + (header -> newList)
        ch      = inputStream.read();
        done    = ch == -1
      }
      else {
        done = true
      }
    }
    Right(headers)
  }

  private def parseBody(inputStream : InputStream, 
                        headers     : Map[String,List[String]]) : Either[HTTPRequestParseError,Option[Array[Byte]]] = {
    val ch = inputStream.read();
    headers.get("content-length") match {
      case None if ch != -1 => Left(HTTPRequestParseError("Expecting a 'content-length' header"))
      case None if ch == -1 => Right(None)
      case Some(lengthString :: rest) => {
        val length = lengthString.toInt
        val buffer = new Array[Byte](length)
        buffer(0) = ch.toByte
        for (i <- 1 until length) {
          buffer(i) = inputStream.read.toByte
        }
        Right(Some(buffer))
      }
    }
  }
}
