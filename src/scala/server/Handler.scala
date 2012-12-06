package bim.server

/** Handles a parsed request */
trait Handler {
  def handle(request:Either[HTTPRequestParseError,HTTPRequest]) : HTTPResponse
}
