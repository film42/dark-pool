package darkpool.server

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import spray.http.MediaTypes._
import spray.routing.HttpService

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by: film42 on: 3/14/15.
 */

package object routes {
  import darkpool.models.TradingJsonProtocol._
  import spray.json._

  trait ApiService extends HttpService {

    implicit val timeout = Timeout(5.seconds)

    def actorResponse[T](actor: ActorRef, msg: Any): Future[T] = (actor ? msg).mapTo[T]

    def actorWithName(name: String): ActorRef = ???

    def routes =
      path("snapshot") {
        get {
          respondWithMediaType(`application/json`) {
            //onComplete(actorResponse(actorWithName("engine"), Snapshot)) {
              complete {
                List(1,2,3).toJson.toString()
              }
            //}
          }
        }
      } ~ path("orders" / Segment) { userId =>
        get {
          respondWithMediaType(`application/json`) {
            complete {
              s"""{"test" : "$userId"}"""
            }
          }
        }
      }

  }


  class ApiActor extends Actor with ApiService {

    // The HttpService trait defines only one abstract member, which
    // connects the services environment to the enclosing actor or test
    def actorRefFactory = context

    // This actor only runs our route, but you could add
    // other things here, like request stream processing
    // or timeout handling
    def receive = runRoute(routes)
  }
}
