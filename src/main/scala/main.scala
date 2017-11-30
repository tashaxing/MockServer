import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

object MockServer
{
    final case class Item(name: String, id: Int)
    implicit val itemFormat == jsonFormat1

    def main(args: Array[String]): Unit =
    {
        implicit val system = ActorSystem("mock_system")
        implicit val materializer = ActorMaterializer()
        // needed for the future flatMap/onComplete in the end
        implicit val executionContext = system.dispatcher

        val route =
            path("test") {
                get {
                    complete("just a test for scala http")
                }
            }

        val bindingFuture = Http().bindAndHandle(route, "localhost", 7070)

        println(s"Server online at http://localhost:7070/\nPress RETURN to stop...")
        StdIn.readLine() // let it run until user presses return

        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }

}
