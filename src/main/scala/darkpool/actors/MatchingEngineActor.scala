package darkpool.actors

import akka.actor.{Actor, ActorLogging}
import darkpool.book.OrderBook
import darkpool.engine.MatchingEngine
import darkpool.engine.commands.{Snapshot, Cancel, Add}
import darkpool.models.orders.{Buy, Sell}

class MatchingEngineActor(buyOrderBook: OrderBook[Buy], sellOrderBook: OrderBook[Sell])
  extends MatchingEngine(buyOrderBook, sellOrderBook) with Actor with ActorLogging {

  override def receive: Receive = {
    case Add(order) => acceptOrder(order)
      log.info(s"Accepting order: $order")
    case Cancel(order) => cancelOrder(order)
      log.info(s"Canceling order: $order")
    case Snapshot =>
      log.info(s"Generating market snapshot")
      context.actorSelection("/user/api") ! marketSnapshot
  }

}
