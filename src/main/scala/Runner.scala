/**
 * Created by: film42 on: 1/14/15.
 */
object Runner extends App {

  def checkArguments(args: Array[String] = args): Boolean = {
    args.size == 3
  }

  // This is just a stub
  println("Usage: server -p [port] -d [persistence] -m [mode]")
  checkArguments()
}
