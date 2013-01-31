package bim.server

object RequestValidationHandler extends Handler {
  def handle(parsedRequest:Either[HTTPRequestParseError,HTTPRequest]) = parsedRequest match {
    case Right(request) => HTTPResponse(status = "200", reasonPhrase = "OK")
    case Left(error)    => HTTPResponse(status = "400", reasonPhrase = "Bad Request")
  }
}
