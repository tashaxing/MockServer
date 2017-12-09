import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}


object WebClient extends App
{
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val host_url = "http://www.easy-mock.com/mock/59ed7cef591f361bb0d95ad8" // this is a mock service
    val get_api = "user"
    val post_api = "login"
    val post_username_param = "mike"
    val post_password_param = "ethhvhe35"

    // url can be rest or non-rest
    def getReq(url: String): Unit =
    {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url, method = HttpMethods.GET))
        responseFuture.onComplete {
                case Success(res) => {
                    println(res)
                    Unmarshal(res.entity).to[String].map { json_str =>
                        Right {
                            // here we can deserialize json string to object or list of object
                            // sth like val res_obj = Json.deserialize([Model])(json_str)
                            // or val res_obj = Json.deserialize(Seq[Model])(json_str)

                            println("get result: ", json_str)
                        }
                    }
                }
                case Failure(error) => println(error.getMessage)
            }
    }

    def postReq(url: String): Unit =
    {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url, method = HttpMethods.POST))
        responseFuture.onComplete {
            case Success(res) => {
                println(res)
                Unmarshal(res.entity).to[String].map { json_str =>
                    Right {
                        // here we can deserialize json string to object or list of object
                        // sth like val res_obj = Json.deserialize([Model])(json_str)
                        // or val res_obj = Json.deserialize(Seq[Model])(json_str)

                        println("post result: ", json_str)
                    }
                }
            }
            case Failure(error) => println(error.getMessage)
        }
    }

    // test get
    getReq(host_url + "/" + get_api)

    // test post
    postReq(s"$host_url/$post_api?username=$post_username_param&password=$post_password_param")
}
