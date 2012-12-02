package bim.server

case class HTTPRequest(val method  : String,
                       val uri     : String,
                       val version : String,
                       val headers : Map[String,String],
                       val body    : Option[Array[Byte]] = None)
