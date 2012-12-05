package bim.server

case class HTTPRequest(val method  : String,
                       val uri     : String,
                       val version : String,
                       val headers : Map[String,List[String]],
                       val body    : Option[Array[Byte]] = None) {
  /** Get the single value for a header.
   * @param headerName name of the header to get
   * @return the only or first value for the header
   */
  def header(headerName:String):Option[String] = headers.get(headerName) match {
    case Some(value :: rest) => Some(value)
    case _ => None
  }
}
