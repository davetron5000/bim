package bim
package server

import java.net._

object NaiveThreadedDispatcher extends Dispatcher {
  class DispatcherThread(socket:Socket) extends Thread {
    override def run = InlineDispatcher.dispatch(socket)
  }
  def dispatch(socket:Socket) = {
    new DispatcherThread(socket).start
  }
}
