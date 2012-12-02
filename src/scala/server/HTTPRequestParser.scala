package bim.server

import java.io.InputStream

object HTTPRequestParser {
  class ParseError extends RuntimeException

  def parse(inputStream:InputStream): Either[HTTPRequestParseError,HTTPRequest] = {
    (for (method  <- readUntil(inputStream,SPACE);
          uri     <- readUntil(inputStream,SPACE);
          version <- parseVersion(inputStream);
          headers <- parseHeaders(inputStream);
          body    <- parseBody(inputStream)) yield HTTPRequest(method,uri,version,headers,body)) match {
      case Some(request) => Right(request)
      case None          => Left(new HTTPRequestParseError())
    }
  }

  private val SPACE:Int = 32
  private val LF:Int = 10
  private val CR:Int = 13
  private val COLON:Int = ':'.toInt

  private def readUntil(inputStream:InputStream, stopChar:Int):Option[String] = {
    val buffer = new StringBuffer(256)
    var ch = inputStream.read()
    while (ch != stopChar) {
      if (ch == -1) {
        return None
      }
      buffer.append(ch.toChar)
      ch = inputStream.read()
    }
    Some(buffer.toString)
  }

  private def parseVersion(inputStream:InputStream):Option[String] = {
    readUntil(inputStream,LF) match {
      case Some(v) if v.startsWith("HTTP/") && v.endsWith("\r") && v.indexOf(".") >= 6 => Some(v.substring(5,v.length() - 1))
      case _ => None
    }
  }

  private def parseHeaders(inputStream:InputStream):Option[Map[String,String]] = {
    var done    : Boolean            = false
    var ch                           = inputStream.read()
    var headers : Map[String,String] = Map()

    while (!done) {
      var key                          = new StringBuffer(80)
      var value                        = new StringBuffer(256)
      var inValue : Boolean            = false

      var prevCh = -1

      while ( (prevCh != CR) && (ch != LF) ) {
        if (ch == -1) return None 
        if (inValue) {
          if ((ch != CR) && (ch != LF)) value.append(ch.toChar)
        }
        else if (ch == COLON) {
          inValue = true
        }
        else {
          if ((ch != CR) && (ch != LF)) key.append(ch.toChar)
        }
        prevCh = ch
        ch = inputStream.read()
      }
      if (key.length() > 0) headers = headers + (key.toString.toLowerCase.trim -> value.toString.trim)
      ch = inputStream.read();
      done = ch == -1
    }
    Some(headers)
  }
  private def parseBody(inputStream:InputStream) = Some(None)
}
