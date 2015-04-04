package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.engine.commands.MarketSnapshot
import darkpool.models.Trade
import darkpool.models.orders._

class MatchingEngine(buyOrderBook: OrderBook[Buy], sellOrderBook: OrderBook[Sell]) {
  // TODO: Expose this via Ledger Actor
  var trades = List[Trade]()

  private var marketReferencePrice: Option[Double] = None

  def referencePrice = marketReferencePrice.getOrElse(Double.PositiveInfinity)

  def referencePrice_=(price: Double) {
    this.synchronized { marketReferencePrice = Some(price) }
  }
  
  def books(orderType: OrderType): (OrderBook[OrderType], OrderBook[OrderType]) = orderType match {
    case BuyOrder => (buyOrderBook, sellOrderBook)
    case SellOrder => (sellOrderBook, buyOrderBook)
  }

  def acceptOrder(order: Order) {
    val (book, counterBook) = books(order.orderType)
    val unfilledOrder = tryMatch(order, counterBook)
    unfilledOrder.map(book.addOrder)
  }

  def cancelOrder(order: Order) {
    val (book, _) = books(order.orderType)
    book.cancelOrder(order)
  }

  def tryMatch(order: Order, counterBook: OrderBook[OrderType]): Option[Order] = {
    if (order.quantity == 0) None
    else counterBook.top match {
      case None => Some(order)
      case Some(top) => tryMatchWithTop(order, top) match {
        // No match, add to order book
        case None => Some(order)
        // A trade was made
        case Some(trade) =>
          counterBook.decreaseTopBy(trade.quantity)
          val unfilledOrder = order.decreasedBy(trade.quantity)

          // Check for self trade prevention:
          // If we find that we're executing two orders of the same account, we will alert any subscribers
          // and continue down with tryMatch
          if(trade.buyerId == trade.sellerId) {
            selfTradePreventionCallback(trade)
          } else {
            tradeCallback(trade)
            this.synchronized { trades = trades :+ trade }
          }

          tryMatch(unfilledOrder, counterBook)
      }
    }
  }

  def marketSnapshot: MarketSnapshot = {
    MarketSnapshot(
      math.abs(sellOrderBook.bestLimit.getOrElse(0.0) - buyOrderBook.bestLimit.getOrElse(0.0)),
      buyOrderBook.thresholdView,
      sellOrderBook.thresholdView,
      referencePrice)
  }

  def ordersForAccountId(accountId: UUID): List[Order] = {
    buyOrderBook.ordersForAccountId(accountId) ++ sellOrderBook.ordersForAccountId(accountId)
  }

  // TODO: Do we need .orderType.isInstanceOf[BuyOrder] ???
  // FIXME: This is nasty code
  private def tryMatchWithTop(order: Order, top: Order): Option[Trade] = {
    def trade(price: Double): Option[Trade] = {
      marketReferencePrice = Some(price)
      val (buy, sell) = if (order.orderType.isInstanceOf[Buy]) (order, top) else (top, order)
      Some(Trade(UUID.randomUUID(), buy.accountId, sell.accountId, buy.id, sell.id, price, math.min(buy.quantity, sell.quantity)))
    }

    lazy val oppositeBestLimit: Option[Double] = {
      val oppositeBook = if (order.orderType.isInstanceOf[Buy]) sellOrderBook else buyOrderBook
      oppositeBook.bestLimit
    }

    (order, top) match {
      // There is a top limit order we can match anything with
      case (_, topLimitOrder @ LimitOrder(_, _, _, _, _)) =>
        if(order.crossesAt(topLimitOrder.threshold)) trade(topLimitOrder.threshold)
        else None

      // Match a limit order with a market order
      case (limitOrder @ LimitOrder(_, _, _, _, _), MarketOrder(_, _, _, _)) =>
        val limitThreshold = oppositeBestLimit match {
          case Some(threshold) => if (order.crossesAt(threshold)) threshold else limitOrder.threshold
          case None => limitOrder.threshold
        }
        trade(limitThreshold)

      // Match a market order with a market order
      case (MarketOrder(_, _, _, _), MarketOrder(_, _, _, _)) =>
        val limitThreshold = oppositeBestLimit match {
          case Some(threshold) => threshold
          case None => marketReferencePrice match {
            case Some(price) => price
            case None => throw new IllegalStateException("Can't execute trade with two market orders without best limit or reference price")
          }
        }
        trade(limitThreshold)
    }
  }

  protected def tradeCallback(trade: Trade) {}
  protected def acceptedOrderCallback(order: Order) {}
  protected def canceledOrderCallback(order: Order) {}
  protected def selfTradePreventionCallback(trade: Trade) {}

}


