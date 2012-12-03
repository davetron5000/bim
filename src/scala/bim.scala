package object bim {
  def unless[A](expression:Boolean)(body: => A):Unit = {
    if (!expression) {
      body
    }
  }
}
