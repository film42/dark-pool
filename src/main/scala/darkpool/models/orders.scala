package darkpool.models

import java.util.UUID

import darkpool.models.common._

package object orders {

  //
  // Order Traits
  //
  trait OrderType
  trait BuyOrder extends OrderType
  trait SellOrder extends OrderType
  trait Order extends Quantity with CreatedAt with ID {
    def decreasedBy(quantity: Double): Order
    def crossesAt(quantity: Double): Boolean
  }
  trait Threshold {
    def threshold: Double
  }

  //
  // Order Implementations
  //
  case class MarketOrder(orderQuantity: Double, orderId: UUID)
    extends Order {

    override def id: UUID = orderId
    override def quantity: Double = orderQuantity
    // Market orders accept any price
    override def crossesAt(quantity: Double): Boolean = true
    override def decreasedBy(quantity: Double): MarketOrder =
      MarketOrder(orderQuantity - quantity, orderId)
  }

  case class LimitOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID)
    extends Order with Threshold {

    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
    // FIXME: Add Buy/ Sell distinction here
    override def crossesAt(quantity: Double): Boolean = {
      false
    }
    override def decreasedBy(quantity: Double): LimitOrder =
      LimitOrder(orderQuantity - quantity, orderThreshold, orderId)
  }

  case class StopOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID)
    extends Order with Threshold {

    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
    // FIXME: Add Buy/ Sell distinction here
    override def crossesAt(quantity: Double): Boolean = {
      false
    }
    override def decreasedBy(quantity: Double): StopOrder =
      StopOrder(orderQuantity - quantity, orderThreshold, orderId)
  }

}