package bim.server

/** Results of parsing where there was an error */
case class HTTPRequestParseError(errorMessage:String) {
  private[this] var throwable:Option[Exception] = None
  def this(t:Exception) = {
    this(t.getMessage)
    throwable = Some(t)
  }
  def exception = throwable
}
