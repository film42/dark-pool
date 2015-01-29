package darkpool.engine

import akka.actor.{ActorSystem, Props}
import darkpool.actors.LedgerActor
import darkpool.book.OrderBook
import darkpool.models.Trade
import darkpool.models.orders._

class MatchingEngine(buyOrderBook: OrderBook[Buy], sellOrderBook: OrderBook[Sell]) {
  private val system = ActorSystem("dark-pool")
  private val ledger = system.actorOf(Props[LedgerActor])

  // TODO: Expose this via Ledger Actor
  var trades = List[Trade]()

  private var marketReferencePrice: Option[Double] = None

  def referencePrice = marketReferencePrice.get

  def referencePrice_=(price: Double) {
    marketReferencePrice = Some(price)
  }

  def getBooks(orderType: OrderType): (OrderBook[OrderType], OrderBook[OrderType]) = orderType match {
    case BuyOrder => (buyOrderBook, sellOrderBook)
    case SellOrder => (sellOrderBook, buyOrderBook)
  }

  def acceptOrder(order: Order) {
    val (book, counterBook) = getBooks(order.orderType)
    val unfilledOrder = tryMatch(order, counterBook)
    unfilledOrder.map(book.add)
  }

  def cancelOrder(order: Order) {
    val (book, _) = getBooks(order.orderType)
    book.cancel(order)
  }

  def tryMatch(order: Order, counterBook: OrderBook[OrderType]): Option[Order] = {
    if (order.quantity == 0) None
    else counterBook.top match {
      case None => Some(order)
      case Some(top) => tryMatchWithTop(order, top) match {
        case None => Some(order)
        case Some(trade) =>
          counterBook.decreaseTopBy(trade.quantity)
          // ledger ! trade // FIXME: don't send to akka right now
          trades = trades :+ trade
          val unfilledOrder = order.decreasedBy(trade.quantity)
          tryMatch(unfilledOrder, counterBook)
      }
    }
  }

  // TODO: Do we need .orderType.isInstanceOf[BuyOrder] ???
  // FIXME: This is nasty code
  private def tryMatchWithTop(order: Order, top: Order): Option[Trade] = {
    def trade(price: Double): Option[Trade] = {
      marketReferencePrice = Some(price)
      val (buy, sell) = if (order.orderType.isInstanceOf[Buy]) (order, top) else (top, order)
      Some(Trade(buy.id, sell.id, price, math.min(buy.quantity, sell.quantity)))
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

}


