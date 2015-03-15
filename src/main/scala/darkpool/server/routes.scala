package darkpool.server

import akka.actor.{Actor, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import darkpool.engine.commands._
import spray.http.MediaTypes._
import spray.routing.HttpService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by: film42 on: 3/14/15.
 */

package object routes {
  import darkpool.models.TradingJsonProtocol._
  import spray.json._

  trait ApiService extends HttpService {

    implicit val timeout = Timeout(5.seconds)

//    def actorResponse[T](actor: ActorRef, msg: Any): Future[T] = (actor ? msg).mapTo[T]

    def actorWithName(name: String): ActorSelection

    def routes =
      path("snapshot") {
        get { ctx =>
            ask(actorWithName("engine"), Snapshot)
              .mapTo[MarketSnapshot]
              .onComplete {

              case Success(marketSnapshot) =>
                ctx.complete(marketSnapshot.toJson.toString())
              case Failure(ex) =>
                ctx.complete(ex.getMessage)
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
    override def actorRefFactory = context

    // This actor only runs our route, but you could add
    // other things here, like request stream processing
    // or timeout handling
    override def receive = runRoute(routes)

    override def actorWithName(name: String): ActorSelection =
      context.actorSelection(s"/user/$name")

  }
}
