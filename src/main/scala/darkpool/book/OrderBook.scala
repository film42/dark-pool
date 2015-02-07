package darkpool.book

import java.util.UUID

import darkpool.models.common.ThresholdQuantity
import darkpool.models.orders._

import scala.Function.tupled

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

  def addOrder(order: Order) {
    order match {
      case limitOrder @ LimitOrder(_, _, _, _, _) => addOrderWithThreshold(limitOrder)
      case MarketOrder(_, _, _, _) => this.synchronized { marketBook = marketBook :+ order }
      case _ => throw new IllegalArgumentException("Order not supported!")
    }
  }

  def canceledOrders: List[Order] = cancelBook

  def top: Option[Order] = marketBook match {
    case head :: _ => Some(head)
    case _ => limitBook.headOption.map({
      case (_, orders) => orders.head
    })
  }

  def contains(order: Order): Boolean = {
    // TODO: Can we make this faster?
    orders.contains(order)
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
        val newLimitBook = (quantity == top.quantity, rest.isEmpty) match  {
          case (true, true) => tail
          case (true, false) => (bookLevel, rest) :: tail
          case _ => (bookLevel, top.decreasedBy(quantity) :: rest) :: tail
        }
        this.synchronized { limitBook = newLimitBook }
      case _ => throw new IllegalStateException("Bad state trying to decrease top of limit book!")
    }

    // Match for Market Orders, then Limit Orders
    marketBook match {
      case top :: tail =>
        val newMarketBook =
          if (quantity == top.quantity) tail
          else top.decreasedBy(quantity) :: tail
        this.synchronized { marketBook = newMarketBook }
      case _ => decreaseTopOfLimitBook()
    }
  }

  def ordersForAccountId(accountId: UUID): List[Order] = {
    marketBook ++ (limitBook map tupled { (threshold: Double, list: List[Order]) =>
      list.filter(_.accountId == accountId)
    } flatMap(o => o))
  }

  // NOTE: We can't cancel using apply() because quantities may change
  def cancelOrder(order: Order) {
    def removeFromThresholdList(book: List[ThresholdType]): (Option[Order], List[ThresholdType]) = {
      val thresholdOrder = order.asInstanceOf[Order with Threshold]
      book.find(_._1 == thresholdOrder.threshold) match {
        case Some((threshold, pendingOrders)) =>
          // TODO: Merge these two sections into something much cleaner!
          // 1) Save the canceled order
          val orderOption = pendingOrders.partition(o => o.id == order.id) match {
            case (canceledOrder :: tail, rest) => Some(canceledOrder)
            case _ => None
          }
          // 2) Remove it from book
          val cleanBook = book map tupled { (t: Double, l: List[Order]) =>
            (t, l.filter(o => o.id != order.id)) match {
              case (_, Nil) => None
              case thresholdLevel => Some(thresholdLevel)
            }
          } flatMap (o => o)
          // 3) Return the tuple
          (orderOption, cleanBook)

        case None => (None, book)
      }
    }

    // Match on order
    order match {
      // Cancel limit order if possible
      case limitOrder@LimitOrder(_, _, _, _, _) =>
        // Find threshold and orders at that threshold
        val (cancelOrder, book) = removeFromThresholdList(limitBook)
        this.synchronized {
          // Save the book (could be changed)
          limitBook = book
          // Add the canceled order to the canceled orders list
          cancelBook = cancelBook ++ List(cancelOrder).flatten
        }

      // Cancel market order if possible
      case MarketOrder(_, _, _, _) => marketBook.partition(o => o.id != order.id) match {
        case (orders, canceledOrder :: Nil) =>
          this.synchronized {
            marketBook = orders
            cancelBook = cancelBook :+ canceledOrder
          }
        case _ => // Do nothing
      }

      // No such order
      case _ => throw new IllegalArgumentException("Unknown order!")
    }
  }

  def thresholdView: List[ThresholdQuantity] = {
    limitBook map tupled { (threshold, orderList) =>
      val totalQuantity = orderList.foldLeft(0.0)(_ + _.quantity)
      ThresholdQuantity(threshold,totalQuantity)
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
      case LimitOrder(_, _, _, _, _) => this.synchronized { limitBook = insert(limitBook) }
      case StopOrder(_, _, _, _, _) => throw new NotImplementedError("Not implemented")
      case _ => throw new IllegalArgumentException("No such threshold type order!")
    }
  }
}
