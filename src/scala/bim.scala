package object bim {
  def unless[A](expression:Boolean)(body: => A):Unit = {
    if (!expression) {
      body
    }
  }

  implicit class Tapper[A](obj:A) {
    def tap[B](code: A => B) = {
      code(obj)
      obj
    }
  }
}
