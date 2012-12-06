package bim
package server

import java.io.InputStream

/** Parses an HTTP Request */
object HTTPRequestParser {
  /**
   * parse the given InputStream as an HTTP request.
   *
   * @param inputStream input stream containing bytes that might be an HTTP request
   *
   * @return If a `Right`, the request was parsed successfully.  If a `Left`, there was a problem
   */
  def parse(inputStream:InputStream): Either[HTTPRequestParseError,HTTPRequest] = {
    for (methodURIAndVersion <- readRequestLine(inputStream).right;
         headers <- parseHeaders(inputStream).right;
         body    <- parseBody(inputStream,headers).right) yield HTTPRequest(methodURIAndVersion._1,
                                                                            methodURIAndVersion._2,
                                                                            methodURIAndVersion._3,
                                                                            headers,
                                                                            body)
  }

  private def readLine(inputStream:InputStream):Either[HTTPRequestParseError,String] = {
    val buffer = new StringBuffer(256)
    var ch = inputStream.read
    while (ch != CR && ch !=  -1) {
      buffer.append(ch.toChar)
      ch = inputStream.read
    }
    if (ch == CR) {
      ch = inputStream.read
      if (ch != -1 && ch != LF)
        return Left(new HTTPRequestParseError("Got CR, but not LF"))
    }
    Right(buffer.toString())
  }

  private def readRequestLine(inputStream:InputStream):Either[HTTPRequestParseError,(String,String,String)] = {
    readLine(inputStream).fold(
      error => Left(error),
      parsedLine => parsedLine.split(" ").toList match {
        case method :: uri :: version :: rest if version.startsWith("HTTP/") => Right((method,uri,version.substring(5)))
        case method :: uri :: version :: rest => Left(new HTTPRequestParseError(s"version string $version is invalid"))
        case method :: uri :: Nil             => Right((method,uri,"1.0"))
        case _                                => Left(new HTTPRequestParseError("request line missing uri and version"))
      }
    )
  }

  private val SPACE : Int = ' '.toInt
  private val LF    : Int = '\n'.toInt
  private val CR    : Int = '\r'.toInt
  private val COLON : Int = ':'. toInt

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
    headers.get("content-length") match {
      case Some(lengthString :: rest) => {
        val ch = inputStream.read();
        val length = lengthString.toInt
        val buffer = new Array[Byte](length)
        buffer(0) = ch.toByte
        for (i <- 1 until length) {
          buffer(i) = inputStream.read.toByte
        }
        Right(Some(buffer))
      }
      case _ => Right(None)
    }
  }
}
