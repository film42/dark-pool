package darkpool.tools

import java.util.UUID

import akka.actor._
import com.github.nscala_time.time.Imports._
import darkpool.actors.{MatchingEngineActor, QueryActor}
import darkpool.book.OrderBook
import darkpool.engine.commands.Add
import darkpool.models.Trade
import darkpool.models.orders._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random


object RobotTrader extends App {
  import ExecutionContext.Implicits.global

  class RobotLedger extends Actor with ActorLogging {
    var timeOfLastTrade = DateTime.now

    override def receive: Receive = {
      case trade @ Trade(_, _, _, _, _, _) =>
        val elapsedTime = trade.createdAt.getMillis - timeOfLastTrade.getMillis
        log.info(s"Time since last trade: $elapsedTime ms: $trade")
        this.synchronized { timeOfLastTrade = trade.createdAt }
    }
  }


  class TradeGenerator extends Actor {
    val random = new Random()

    override def receive: Actor.Receive = {
      case _ => generateOrder
    }

    private def generateOrder {
      val engineActor = context.actorSelection("../engineActor")

      engineActor ! Add(randomOrder)
    }

    private def randomThreshold = 10 + (random.nextInt(10) / 10.0)
    private def randomQuantity = random.nextInt(1000) + 1
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

  val orderBookBuy = new OrderBook(BuyOrder)
  val orderBookSell = new OrderBook(SellOrder)

  val engineActor = system.actorOf(Props(new MatchingEngineActor(orderBookBuy, orderBookSell)), "engineActor")
  val apiActor = system.actorOf(Props[QueryActor], "api")
  val ledgerActor = system.actorOf(Props[RobotLedger], "ledger") // New ledger actor
  val tradeGenerator = system.actorOf(Props[TradeGenerator], "robot")

  system.scheduler.schedule(0 milliseconds, 50 milliseconds) {
    tradeGenerator ! "generate trade"
  }

}
