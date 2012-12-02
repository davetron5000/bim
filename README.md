# bim - A Scala Web Server and Application Framework

bim is a way to write a web application in Scala.  It does not depend on JEE/Servlets, and is designed to allow programming at
various levels of abstraction, from processing raw HTTP requests to a full-fledged application framework.

bim follows convention over configuration, aiming to make the easy things simple and the difficult things possible.

## HTTP Server

At the heart of bim is the HTTP server.  It's job is merely to parse HTTP requests and deliver HTTP responses.  This is done
using a pool of threads to handle the requests.  This allows request-handling code to be implemented as plainly as possible,
without worrying about blocking.  In other words, we're specifically decidig to avoid "callback soup".

## Request Handling

The HTTP server, upon parsing a request, will deliver that request to a reqest handler for processing.  Various request handlers
can be configured.

### Static File Server

A static file serving request handler is provided that allows bim to act as a vanilla web server.

