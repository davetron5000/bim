package bim.server

case class HTTPResponse(version      : String                   = "1.1",
                        status       : String,
                        reasonPhrase : String,
                        headers      : Map[String,List[String]] = Map(),
                        body         : Option[Array[Byte]]      = None)
