import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

import scala.concurrent.Future

object MockServer
{
    // model
    final case class User(name: String, age: Int, addr: String)
    implicit val itemFormat = jsonFormat3(User) // jsonFormatX refers to X number arguments

    final case class UserGroup(items: List[User])
    implicit val orderFormat = jsonFormat1(UserGroup)

    // http api
//    def fetchUserGroup(): Future[Option[List[User]]] =
//    {
//
//    }
//
//    def fetchUser(user_id: Int): Future[Option[User]] =
//    {
//
//    }
//
//    def saveUser(user: User): Future[Done] =
//    {
//
//    }

    def main(args: Array[String]): Unit =
    {
        implicit val system = ActorSystem("mock_system")
        implicit val materializer = ActorMaterializer()

        // needed for the future flatMap/onComplete in the end
        implicit val executionContext = system.dispatcher

        val route: Route =
            get
            {
                pathPrefix("")
                {
                    complete("the index response")
                }
                pathPrefix("list_all")
                {
                    val user_group = List(User("jack", 18, "NewYork"), User("mike", 21, "paris"))
                    complete(user_group)
                }
                pathPrefix("user" / LongNumber)
                {
                    id => complete(User("lucy", id.toInt, "tokyo"))
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
