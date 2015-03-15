package darkpool.server

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import darkpool.actors.{LedgerActor, MatchingEngineActor, QueryActor}
import darkpool.book.OrderBook
import darkpool.models.orders.{BuyOrder, SellOrder}
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * Created by: film42 on: 3/14/15.
 */
object WebServer extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("dark-pool-server")

  // create and start our service actor
  val service = system.actorOf(Props[routes.ApiActor], "api-service")

  // setup the matching engine
  val orderBookBuy = new OrderBook(BuyOrder)
  val orderBookSell = new OrderBook(SellOrder)
  val engineActor = system.actorOf(Props(new MatchingEngineActor(orderBookBuy, orderBookSell)), "engine")
  val apiActor = system.actorOf(Props[QueryActor], "api")
  val ledgerActor = system.actorOf(Props[LedgerActor], "ledger")

  // timeout default
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
