package darkpool.server

import java.util.UUID

import akka.actor.{ActorLogging, Actor, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import darkpool.datastore.LedgerDatastore
import darkpool.engine.commands._
import darkpool.models._
import darkpool.models.orders.Order
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.routing
import spray.routing.{StandardRoute, HttpService, RequestContext}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

/**
 * Created by: film42 on: 3/14/15.
 */

package object routes {
  import darkpool.models.TradingJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  import spray.json._

  trait ApiService extends HttpService {

    implicit val timeout = Timeout(5.seconds)

    val jsonResponse = respondWithMediaType(`application/json`)

    def completeFromActor[A](actorName: String, message: Any)(implicit ctx: RequestContext, tag: ClassTag[A]) = {
      responseFromActor[A, Unit](actorName, message) { json =>
        ctx.complete(json)
      }
    }

    def responseFromActor[A, B](actorName: String, message: Any)(f: (String => B))(implicit tag: ClassTag[A]): B = {
      // This is terrible because we're now blocking the future
      val blockingResult = Await.result(ask(actorWithName("engine"), message).mapTo[A], 5 seconds)

      blockingResult match {
        case marketSnapshot: MarketSnapshot =>
          f(marketSnapshot.toJson.toString())
        case OrderAdded =>
          f(OrderAdded.toJson.toString())
        case OrderNotAdded =>
          f(OrderNotAdded.toJson.toString())
        case o: OrderCanceled =>
          f(o.toJson.toString())
        case OrderNotCanceled =>
          f(OrderNotCanceled.toJson.toString())
        case orderForAccountResponse: OrdersForAccountResponse =>
          f(orderForAccountResponse.toJson.toString())
        case ex: String =>
          f(Error(ex).toJson.toString())
      }
    }

    def actorWithName(name: String): ActorSelection

    def routes = jsonResponse {

      path("snapshot") {
        get { implicit ctx =>
          completeFromActor[MarketSnapshot]("engine", Snapshot)
        }
      } ~
      pathPrefix("orders") {
        pathEndOrSingleSlash {
          post {
            entity(as[String]) { complete(_) }
          }
        } ~
        path("account" / Segment) { accountId =>
          get { implicit ctx =>
            val uuid = UUID.fromString(accountId)
            completeFromActor[OrdersForAccountResponse]("engine", OrdersForAccount(uuid))
          }
        } ~
        path("add") {
          post {
            entity(as[Order]) { order =>
              responseFromActor[AddOrderResponse, StandardRoute]("engine", Add(order)) { json =>
                complete { json }
              }
            }
          }
        } ~
        path("cancel") {
          post {
            entity(as[Order]) { order =>
              responseFromActor[CancelOrderResponse, StandardRoute]("engine", Cancel(order)) { json =>
                complete { json }
              }
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
        path("account" / Segment) { accountId =>
          get {
            parameters('limit ? 100)  { limitParameter =>
              val uuid = UUID.fromString(accountId)
              complete(LedgerDatastore.filterByAccountId(uuid).take(limitParameter).toJson.toString())
            }
          }
        } ~
        path(Segment) { orderId =>
          get {
            val uuid = UUID.fromString(orderId)
            val optionTrade = LedgerDatastore.find(uuid)

            optionTrade match {
              case Some(trade) =>
                complete(trade.toJson.toString())
              case None =>
                complete(Error(s"No such trade with id $orderId").toJson.toString())
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
