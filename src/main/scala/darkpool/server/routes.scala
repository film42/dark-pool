package darkpool.server

import java.util.UUID

import akka.actor.{Actor, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import darkpool.datastore.LedgerDatastore
import darkpool.engine.commands._
import darkpool.models._

import spray.http.MediaTypes._
import spray.routing.{HttpService, RequestContext}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by: film42 on: 3/14/15.
 */

package object routes {
  import TradingJsonProtocol._
  import spray.json._

  trait ApiService extends HttpService {

    implicit val timeout = Timeout(5.seconds)

    val jsonResponse = respondWithMediaType(`application/json`)

    def responseFromActor[A](actorName: String, message: Any)(implicit ctx: RequestContext) = {
      ask(actorWithName("engine"), Snapshot)
        .mapTo[MarketSnapshot]
        .onComplete {

        case Success(marketSnapshot) =>
          ctx.complete(marketSnapshot.toJson.toString())
        case Failure(ex) =>
          ctx.complete(Error(ex.getMessage).toJson.toString())
      }
    }

    def actorWithName(name: String): ActorSelection

    def routes = jsonResponse {

      path("snapshot") {
        get { implicit ctx =>
          responseFromActor[MarketSnapshot]("engine", Snapshot)
        }

      } ~
      pathPrefix("orders") {
        pathEndOrSingleSlash {
          post {
            entity(as[String]) { complete(_) }
          }
        } ~
        path(Segment) { userId =>
          get {
            complete {
              s"""{"test" : "$userId"}"""
            }
          }
        }
      } ~
      pathPrefix("trades") {
        pathEndOrSingleSlash {
          get {
            parameters('limit ? 100)  { limitParameter =>
              complete(LedgerDatastore.limit(limitParameter).toJson.toString())
            }
          }
        } ~
        path(Segment) { someId =>
          get {
            val uuid = UUID.fromString(someId)
            val optionTrade = LedgerDatastore.find(uuid)

            optionTrade match {
              case Some(trade) =>
                complete(trade.toJson.toString())
              case None =>
                complete(Error(s"No such trade with id $someId").toJson.toString())
            }
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
