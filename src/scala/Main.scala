import foo._

object main extends App {
  implicit class IntWithTimes(val n:Int) extends Times

  10.times { i => Console.println(s"Got $i!") }
}
