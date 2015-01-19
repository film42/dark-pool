package darkpool.models

import darkpool.models.orders._

package object book {
  class OrderBook[O <: OrderType](ob: O) {
    private type ThresholdType = (Double, List[Order])

    private var marketBook: List[Order] = Nil
    private var limitBook: List[ThresholdType] = Nil
    private val priceOrdering = if(this.isInstanceOf[SellOrder]) Ordering[Double] else Ordering[Double].reverse

    def add(order: Order) {
      order match {
        case limitOrder @ LimitOrder(_, _, _) => addOrderWithThreshold(limitOrder)
        case MarketOrder(_, _) => marketBook = marketBook :+ order
        case _ => throw new IllegalArgumentException("Order not supported!")
      }
    }

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
        case LimitOrder(_, _, _) => limitBook = insert(limitBook)
        case _ => throw new IllegalArgumentException("No such threshold type order!")
      }
    }


  }
}
