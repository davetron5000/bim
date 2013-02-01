package bim.apps

import java.net._

import bim.server._

object EverythingsOKApp  {
  def main(args: Array[String]):Unit = {
    val port       = args.lift(0).getOrElse("8080").toInt
    val host       = args.lift(1).map { InetAddress.getByName(_) }.getOrElse(InetAddress.getLocalHost)
    val dispatcher = args.lift(2) match {
      case Some("threaded") => NaiveThreadedDispatcher
      case _                => InlineDispatcher
    }

    val server = new HTTPServer(port        = port,
                                inetAddress = host,
                                timeout     = 4000,
                                dispatcher  = dispatcher)

    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run:Unit = {
        println("Shutdown hook!")
        server.stop
      }
    })

    server.start
  }
}
