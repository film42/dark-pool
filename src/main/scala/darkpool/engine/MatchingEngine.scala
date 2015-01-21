package darkpool.engine

import akka.actor.{ActorSystem, Props}
import darkpool.actors.LedgerActor
import darkpool.book.OrderBook
import darkpool.models.Trade
import darkpool.models.orders._

class MatchingEngine(buyOrderBook: OrderBook[BuyOrder], sellOrderBook: OrderBook[SellOrder]) {
  val system = ActorSystem("dark-pool")
  val ledger = system.actorOf(Props[LedgerActor])

  private var _referencePrice: Option[Double] = None

  def referencePrice = _referencePrice.get

  def referencePrice_=(price: Double) {
    _referencePrice = Some(price)
  }

  def getBooks(orderType: OrderType): (OrderBook[OrderType], OrderBook[OrderType]) = orderType match {
    case _: BuyOrder => (buyOrderBook, sellOrderBook)
    case _: SellOrder => (sellOrderBook, buyOrderBook)
  }

  def acceptOrder(order: Order) {
    val (book, counterBook) = getBooks(order.orderType)
    val unfilledOrder = tryMatch(order, counterBook)
    unfilledOrder.map(book.add)
  }

  def tryMatch(order: Order, counterBook: OrderBook[OrderType]): Option[Order] = {
    if (order.quantity == 0) None
    else counterBook.top match {
      case None => Some(order)
      case Some(top) => tryMatchWithTop(order, top) match {
        case None => Some(order)
        case Some(trade) =>
          counterBook.decreaseTopBy(trade.quantity)
          ledger ! trade
          val unfilledOrder = order.decreasedBy(trade.quantity)
          tryMatch(unfilledOrder, counterBook)
      }
    }
  }

  // TODO: Do we need .orderType.isInstanceOf[BuyOrder] ???
  // FIXME: This is nasty code
  private def tryMatchWithTop(order: Order, top: Order): Option[Trade] = {
    def trade(price: Double): Option[Trade] = {
      _referencePrice = Some(price)
      val (buy, sell) = if (order.orderType.isInstanceOf[BuyOrder]) (order, top) else (top, order)
      Some(Trade(buy.id, sell.id, price, math.min(buy.quantity, sell.quantity)))
    }

    lazy val oppositeBestLimit: Option[Double] = {
      val oppositeBook = if (order.orderType.isInstanceOf[BuyOrder]) sellOrderBook else buyOrderBook
      oppositeBook.bestLimit
    }

    (order, top) match {
      // There is a top limit order we can match anything with
      case (_, topLimitOrder @ LimitOrder(_, _, _)) =>
        if(order.crossesAt(topLimitOrder.threshold)) trade(topLimitOrder.threshold)
        else None

      // Match a limit order with a market order
      case (limitOrder @ LimitOrder(_, _, _), MarketOrder(_, _)) =>
        val limitThreshold = oppositeBestLimit match {
          case Some(threshold) => if (order.crossesAt(threshold)) threshold else limitOrder.threshold
          case None => limitOrder.threshold
        }
        trade(limitThreshold)

      // Match a market order with a market order
      case (MarketOrder(_, _), MarketOrder(_, _)) =>
        val limitThreshold = oppositeBestLimit match {
          case Some(threshold) => threshold
          case None => _referencePrice match {
            case Some(price) => price
            case None => throw new IllegalStateException("Can't execute trade with two market orders without best limit or reference price")
          }
        }
        trade(limitThreshold)
    }
  }

}


