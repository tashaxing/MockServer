import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.HttpOriginRange
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.collection.mutable

object WebServer
{
    // cors setting for other origin access
    val settings = CorsSettings.defaultSettings.copy(
        allowedOrigins = HttpOriginRange.* // * refers to all
    )

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

    private val userGroup = mutable.ListBuffer[User]()

    def main(args: Array[String]): Unit =
    {
        implicit val system = ActorSystem("mock_system")
        implicit val materializer = ActorMaterializer()

        // needed for the future flatMap/onComplete in the end
        implicit val executionContext = system.dispatcher

        // define routes, notice there are many ways
        val route: Route =
            (path("hello") & get & cors(settings))
            {
                complete("hello akka")

            } ~
            (path("list_all") & cors(settings))
            {
                // simple get
                get
                {
                    // add elem to userGroup, can use this.userGroup += User("jack", 18, "NewYork")
                    userGroup.clear()
                    userGroup += User("jack", 18, "NewYork")
                    userGroup += User("mike", 21, "paris")
                    val user_group = UserGroup(this.userGroup.toList)
                    complete(user_group)
                }
            } ~
            get
            {
                // get by params using akka http path matcher
                (pathPrefix("user" / IntNumber ) & cors(settings))
                {
                    age =>
                    {
                        val user = User("lucy", age.toInt, "tokyo")
                        complete(user)
                    }
                }
            } ~
            post {
                (path("create_user") & cors(settings))
                {
                    entity(as[User])
                    {
                        user =>
                        {
                            println(user)
                            // do sth to process
                            userGroup += user
                            println(userGroup)

                            complete("done")
                        }
                    }
                }
            }


        // bind to ip and port and start server
        val bindingFuture = Http().bindAndHandle(route, "localhost", 7070)

        println(s"Server online at http://localhost:7070/\nPress RETURN to stop...")
        StdIn.readLine() // let it run until user presses return
//        while (1) // or use a dead loop

        // stop server
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }

}
