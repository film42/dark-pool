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
    def crossesAt(price: Double): Boolean
    // TODO: Rename this awful name
    def orderType: OrderType = this match {
      case order: BuyOrder => new BuyOrder {}
      case order: SellOrder => new SellOrder {}
      case _ => throw new IllegalStateException("Unknown OrderType")
    }
  }
  trait Threshold {
    def threshold: Double
  }

  //
  // Order Implementations
  //
  case class MarketOrder[+Type](orderQuantity: Double, orderId: UUID)
    extends Order {

    override def id: UUID = orderId
    override def quantity: Double = orderQuantity
    // Market orders accept any price
    override def crossesAt(price: Double): Boolean = true
    override def decreasedBy(quantity: Double): MarketOrder[Type] =
      MarketOrder[Type](orderQuantity - quantity, orderId)
  }

  case class LimitOrder[+Type](orderQuantity: Double, orderThreshold: Double, orderId: UUID)
    extends Order with Threshold {

    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
    override def crossesAt(price: Double): Boolean = this match {
      case order: BuyOrder => price <= threshold
      case order: SellOrder => price >= threshold
      case _ => throw new IllegalStateException("Unknown OrderType")
    }
    override def decreasedBy(quantity: Double): LimitOrder[Type] =
      LimitOrder[Type](orderQuantity - quantity, orderThreshold, orderId)
  }

  case class StopOrder[+Type](orderQuantity: Double, orderThreshold: Double, orderId: UUID)
    extends Order with Threshold {

    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
    // FIXME: Check code
    override def crossesAt(price: Double): Boolean = this match {
      case order: BuyOrder => price <= threshold
      case order: SellOrder => price >= threshold
      case _ => throw new IllegalStateException("Unknown OrderType")
    }
    override def decreasedBy(quantity: Double): StopOrder[Type] =
      StopOrder[Type](orderQuantity - quantity, orderThreshold, orderId)
  }

  case class CancelOrder[+Type](orderId: UUID)
    extends Order {

    override def decreasedBy(quantity: Double): CancelOrder[Type] = CancelOrder[Type](orderId)
    override def crossesAt(price: Double): Boolean = true
    override def quantity: Double = 0.0
    override def id: UUID = orderId
  }

}