package bim.server

object EverythingsOKHandler extends Handler {
  def handle(request:Either[HTTPRequestParseError,HTTPRequest]) = HTTPResponse(status = "200", reasonPhrase = "OK")
}
