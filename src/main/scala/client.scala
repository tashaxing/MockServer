import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{ Failure, Success }


object WebClient extends App
{
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val host_url = "http://www.easy-mock.com/mock/59ed7cef591f361bb0d95ad8"
    val get_api = "user"
    val post_api = "login"
    val post_username_param = "mike"
    val post_password_param = "ethhvhe35"

    def getReq(url: String): Unit =
    {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
        responseFuture.onComplete {
                case Success(res) => {
                    println(res)
                }
                case Failure(error) => println(error)
            }
    }

    def postReq(url: String): Unit =
    {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
        responseFuture.onComplete {
            case Success(res) => println(res)
            case Failure(error) => println(error)
        }
    }

    // test get
    getReq(host_url + "/" + get_api)

    // test post
    postReq(s"$host_url/$post_api?username=$post_username_param&password=$post_password_param")
}
