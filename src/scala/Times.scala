package foo

trait Times {
  val n:Int

  def times( f: (Int) => Unit ):Unit = {
    for (i <- 0 until n) { f(i) }
  }
}


// vim: set ts=2 sw=2 et:
