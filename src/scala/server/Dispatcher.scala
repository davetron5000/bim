package bim.server

import java.net._

/** Dispatches a request */
trait Dispatcher {
  def dispatch(socket:Socket)
}
