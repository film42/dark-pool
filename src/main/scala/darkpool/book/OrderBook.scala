package darkpool.book

import darkpool.models.orders._
import Function.tupled

class OrderBook[+O <: OrderType](orderType: OrderType) {
  private type ThresholdType = (Double, List[Order])

  private var marketBook: List[Order] = Nil
  private var limitBook: List[ThresholdType] = Nil
  private var cancelBook: List[Order] = Nil
  private val priceOrdering = orderType match {
    case SellOrder => Ordering[Double]
    case BuyOrder => Ordering[Double].reverse
    case _ => throw new IllegalStateException("Unknown priceOrdering type for Book.")
  }

  def add(order: Order) {
    order match {
      case limitOrder @ LimitOrder(_, _, _, _) => addOrderWithThreshold(limitOrder)
      case MarketOrder(_, _, _) => marketBook = marketBook :+ order
      case _ => throw new IllegalArgumentException("Order not supported!")
    }
  }

  def cancel(order: Order) {
    cancelOrder(order)
  }

  def canceledOrders: List[Order] = cancelBook

  def top: Option[Order] = marketBook match {
    case head :: _ => Some(head)
    case _ => limitBook.headOption.map({
      case (_, orders) => orders.head
    })
  }

  def orders: List[Order] = marketBook ::: limitBook.flatMap({
    case (_, orders) => orders
  })

  // Slight nomenclature change!
  def bestLimit: Option[Double] = limitBook.headOption.map({
    case (threshold, _) => threshold
  })

  def decreaseTopBy(quantity: Double) {
    def decreaseTopOfLimitBook(): Unit = limitBook match {
      case ((bookLevel, orders) :: tail) =>
        val (top :: rest) = orders
        limitBook = (quantity == top.quantity, rest.isEmpty) match  {
          case (true, true) => tail
          case (true, false) => (bookLevel, rest) :: tail
          case _ => (bookLevel, top.decreasedBy(quantity) :: rest) :: tail
        }
      case _ => throw new IllegalStateException("Bad state trying to decrease top of limit book!")
    }

    // Match for Market Orders, then Limit Orders
    marketBook match {
      case top :: tail => marketBook =
        if (quantity == top.quantity) tail
        else top.decreasedBy(quantity) :: tail
      case _ => decreaseTopOfLimitBook()
    }
  }

  //
  // Private Methods
  //
  private def addOrderWithThreshold(order: Order with Threshold) {
    val threshold = order.threshold

    // Not tailrec optimized!
    def insert(list: List[ThresholdType]): List[ThresholdType] = list match {
      // Return new list if none found
      case Nil => List((threshold, List(order)))
      // If list exists, place it at the appropriate bookLevel
      case (head @ (bookLevel, orders)) :: tail => priceOrdering.compare(threshold, bookLevel) match {
        case 0 => (bookLevel, orders :+ order) :: tail
        case n if n < 0 => (threshold, List(order)) :: list
        case _ => head :: insert(tail)
      }
    }

    // Match on order type and update that book
    order match {
      case LimitOrder(_, _, _, _) => limitBook = insert(limitBook)
      case StopOrder(_, _, _, _) => throw new NotImplementedError("Not implemented")
      case _ => throw new IllegalArgumentException("No such threshold type order!")
    }
  }

  // NOTE: We can't cancel using apply() because quantities may change
  private def cancelOrder(order: Order) {
    def removeFromThresholdList(list: List[ThresholdType]): List[ThresholdType] = {
      val thresholdOrder = order.asInstanceOf[Order with Threshold]
      list.find(_._1 == thresholdOrder.threshold) match {
        case Some((threshold, pendingOrders)) =>
          // TODO: Merge these two sections into something much cleaner!
          // 1) Save the canceled order
          pendingOrders.partition(o => o.id == order.id) match {
            case (canceledOrder :: tail, rest) =>
              cancelBook = cancelBook :+ canceledOrder
            case _ => // No match
          }
          // 2) Remove it from list
          list map tupled { (t: Double, l: List[Order]) =>
            (t, l.filter(o => o.id != order.id)) match {
              case (_, Nil) => None
              case thresholdLevel => Some(thresholdLevel)
            }
          } flatMap (o => o)

        case None => list
      }
    }

    // Match on order
    order match {
      // Cancel limit order if possible
      case limitOrder@LimitOrder(_, _, _, _) =>
        // Find threshold and orders at that threshold
        limitBook = removeFromThresholdList(limitBook)

      // Cancel market order if possible
      case MarketOrder(_, _, _) => marketBook.partition(o => o.id != order.id) match {
        case (orders, canceledOrder :: Nil) =>
          marketBook = orders
          cancelBook = cancelBook :+ canceledOrder
        case _ => // Do nothing
      }

      // No such order
      case _ => throw new IllegalArgumentException("Unknown order!")
    }
  }

}
