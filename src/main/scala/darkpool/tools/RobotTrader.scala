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


object RobotTrader {

  case object GenerateOrder

  class TradeGenerator extends Actor with ActorLogging {
    val random = new Random()
    val engine = context.actorSelection("../engine")

    override def receive: Actor.Receive = {
      case GenerateOrder => generateOrder
      case _ =>
    }

    private def generateOrder {
      engine ! Add(randomOrder)
    }

    private def randomThreshold = random.nextInt(100) + (random.nextInt(10) / 10.0)
    private def randomQuantity = random.nextInt(100) + 1
    private def randomOrder: Order = {
      val sideSwitch = random.nextInt(100)

      // Pick a Side
      val side = if(sideSwitch % 2 == 0) {
        BuyOrder
      } else {
        SellOrder
      }

      LimitOrder(side, randomQuantity, randomThreshold, UUID.randomUUID(), UUID.randomUUID())
    }
  }
}
