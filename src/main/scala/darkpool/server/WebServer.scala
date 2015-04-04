package darkpool.server

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import darkpool.actors.{LedgerActor, MatchingEngineActor, QueryActor}
import darkpool.book.OrderBook
import darkpool.engine.commands.Add
import darkpool.models.orders._
import spray.can.Http

import scala.concurrent.duration._
import scala.util.Random
import scala.util.Properties

/**
 * Created by: film42 on: 3/14/15.
 */
object WebServer extends App {

  // TODO: Remove
  object TradeGenerator {
    val random = new Random()

    private def randomThreshold = random.nextInt(100) + (random.nextInt(10) / 10.0)
    private def randomQuantity = random.nextInt(100) + 1

    def randomOrder: Order = {
      val sideSwitch = random.nextInt(100)

      // Pick a Side
      val side = if(sideSwitch % 2 == 0) {
        BuyOrder
      } else {
        SellOrder
      }

      // Always use limit orders for now
      LimitOrder(side, randomQuantity, randomThreshold, UUID.randomUUID(), UUID.randomUUID())
    }
  }
  

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


  // TODO: Remove
  for(i <- Range(0, 1000)) {
    engineActor ! Add(TradeGenerator.randomOrder)
  }


  // timeout default
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  val serverPort = Properties.envOrElse("PORT", "8080").toInt
  println(s"Starting server on port: $serverPort")

  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = serverPort)
}
