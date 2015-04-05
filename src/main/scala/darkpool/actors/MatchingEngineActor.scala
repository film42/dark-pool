package darkpool.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import darkpool.book.OrderBook
import darkpool.engine.MatchingEngine
import darkpool.engine.commands.{Add, Cancel, Snapshot, _}
import darkpool.models.Trade
import darkpool.models.orders.{Buy, Order, Sell}

import scala.util.{Success, Try, Failure}

class MatchingEngineActor(buyOrderBook: OrderBook[Buy], sellOrderBook: OrderBook[Sell])
  extends MatchingEngine(buyOrderBook, sellOrderBook) with Actor with ActorLogging {

  override def receive: Receive = {
    case Add(order) => Try(acceptOrder(order)) match {
      case Success(_) =>
        log.info(s"Accepting order: $order")
        sender ! OrderAdded
      case Failure(exception) =>
        log.info(s"Rejecting order: $order; Exception: ${exception.getMessage}")
        sender ! OrderNotAdded
    }

    case Cancel(order) => cancelOrder(order)
      books(order.orderType)._1.canceledOrders.headOption match {
        case Some(canceledOrder) if canceledOrder.id == order.id =>
          log.info(s"Canceling order: $order")
          sender ! OrderCanceled(canceledOrder)
        case _ =>
          log.info(s"Cannot cancel order: $order")
          sender ! OrderNotCanceled
      }


    case OrdersForAccount(accountId) =>
      log.info(s"Getting orders list for accountId: ${accountId.toString}")
      sender ! OrdersForAccountResponse(ordersForAccountId(accountId))

    case Snapshot =>
      log.info(s"Generating market snapshot")
      sender ! marketSnapshot
  }

  override protected def tradeCallback(trade: Trade) {
    context.actorSelection("/user/ledger") ! trade
  }

  override protected def acceptedOrderCallback(order: Order ) {
    context.actorSelection("/user/ledger") ! order
  }

  override protected def canceledOrderCallback(order: Order) {
    context.actorSelection("/user/ledger") ! order
  }

  override protected def selfTradePreventionCallback(trade: Trade) {
    // TODO: Create a new self trade type?
    context.actorSelection("/user/ledger") ! trade
  }
}
