# HTTP Server

1. Accept socket connection
2. Hand off connection to a worker in a thread pool
3. Worker, using a parser, and a request handler, parses the request and calls the request handler
4. Parser parses the request and returns either the parsed request or some error
5. The request handler, given an error, hands it off to an error handler
6. The request handler, given a parsed request, writes the response to the socket

## ConnectionAcceptor

* accepts incoming socket connections
* Passes those connections to a ConnectionWorker

## ConnectionWorker

* Configured with a RequestParser, an RequestParsingErrorHandler, and a RequestHandler
* RequestParser parsers the request and delegates to the RequestParsingErrorHandler or RequestHandler as needed
* RequestParsingErrorHandler and RequestHandler both return an HTTPResponse, which is used to send data to the socket
* Should support some sort of streaming concept?

## RequestParsingErrorHandler

Used when the request cannot be parsed

## RequestHandler

Basically a function that, given an `HTTPRequest`, returns an `HTTPResponse`

## HTTPRequest

Data structure for whatever is part of the request

## HTTPResponse

Data structure for whatever is to be sent back

