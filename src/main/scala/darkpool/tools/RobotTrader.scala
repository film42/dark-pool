package darkpool.tools

import java.util.UUID

import akka.actor._
import com.github.nscala_time.time.Imports._
import darkpool.actors.{MatchingEngineActor, QueryActor}
import darkpool.book.OrderBook
import darkpool.engine.commands._
import darkpool.models.Trade
import darkpool.models.orders._
import scala.concurrent.duration._

import scala.util.Random


object RobotTrader extends App {

  class RobotLedger extends Actor with ActorLogging {
    var timeOfLastTrade = DateTime.now

    override def receive: Receive = {
      case trade @ Trade(_, _, _, _, _, _) =>
        val elapsedTime = trade.createdAt.getMillis - timeOfLastTrade.getMillis
        log.info(s"Time since last trade: $elapsedTime ms: $trade")
        this.synchronized { timeOfLastTrade = trade.createdAt }
    }
  }

  class TradeGenerator extends Actor with ActorLogging {
    val random = new Random()
    val engine = context.actorSelection("../engineActor")

    override def receive: Actor.Receive = {
      case OrderAdded => // Nothing
      case OrderNotAdded => // Nothing
      case Snapshot => engine ! Snapshot
      case marketSnapshot @ MarketSnapshot(_, _, _, _) =>
        log.info(s"Market Snapshot: $marketSnapshot")
      case _ => generateOrder
    }

    private def generateOrder {
      engine ! Add(randomOrder)
    }

    private def randomThreshold = random.nextInt(100) + (random.nextInt(10) / 10.0)
    private def randomQuantity = random.nextInt(100) + 1
    private def randomOrder: Order = {
      val orderSwitch = random.nextInt(100)
      val sideSwitch = random.nextInt(100)

      // Pick a Side
      val side = if(sideSwitch % 2 == 0) {
        BuyOrder
      } else {
        SellOrder
      }

      // Pick an order
      if(orderSwitch % 2 == 0) {
        MarketOrder(side, randomQuantity, UUID.randomUUID(), UUID.randomUUID())
      } else {
        LimitOrder(side, randomQuantity, randomThreshold, UUID.randomUUID(), UUID.randomUUID())
      }
    }
  }

  val system = ActorSystem("dark-pool-robot-trader")
  // Import execution context
  import system.dispatcher

  val orderBookBuy = new OrderBook(BuyOrder)
  val orderBookSell = new OrderBook(SellOrder)

  val engineActor = system.actorOf(Props(new MatchingEngineActor(orderBookBuy, orderBookSell)), "engineActor")
  val apiActor = system.actorOf(Props[QueryActor], "api")
  val ledgerActor = system.actorOf(Props[RobotLedger], "ledger") // New ledger actor
  val tradeGeneratorActor = system.actorOf(Props[TradeGenerator], "robot")

  // Generate an order every 10ms
  system.scheduler.schedule(0 milliseconds, 1 milliseconds, tradeGeneratorActor, "start")
  // Generate a market snapshot every 10sec
  system.scheduler.schedule(10000 millisecond, 10000 millisecond, tradeGeneratorActor, Snapshot)

}
