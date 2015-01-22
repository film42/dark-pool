package darkpool.models

import java.util.UUID

import darkpool.models.common._

package object orders {

  //
  // Order Traits
  //
  trait OrderType {
    def orderType: OrderType
  }
  trait Buy extends OrderType
  trait Sell extends OrderType
  trait Order extends Quantity with CreatedAt with ID {
    def decreasedBy(quantity: Double): Order
    def crossesAt(price: Double): Boolean
    // TODO: Rename this awful name
    def orderType: OrderType
  }
  trait Threshold {
    def threshold: Double
  }

  case object BuyOrder extends Buy {
    def orderType: OrderType = BuyOrder
  }
  case object SellOrder extends Sell {
    def orderType: OrderType = SellOrder
  }

  //
  // Order Implementations
  //
  case class MarketOrder(orderType: OrderType, orderQuantity: Double, orderId: UUID)
    extends Order {

    override def id: UUID = orderId
    override def quantity: Double = orderQuantity
    // Market orders accept any price
    override def crossesAt(price: Double): Boolean = true
    override def decreasedBy(quantity: Double): MarketOrder =
      MarketOrder(orderType, orderQuantity - quantity, orderId)
  }

  case class LimitOrder(orderType: OrderType, orderQuantity: Double, orderThreshold: Double, orderId: UUID)
    extends Order with Threshold {

    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
    override def crossesAt(price: Double): Boolean = orderType match {
      case BuyOrder => price <= threshold
      case SellOrder => price >= threshold
    }
    override def decreasedBy(quantity: Double): LimitOrder =
      LimitOrder(orderType, orderQuantity - quantity, orderThreshold, orderId)
  }

  case class StopOrder(orderType: OrderType, orderQuantity: Double, orderThreshold: Double, orderId: UUID)
    extends Order with Threshold {

    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
    override def crossesAt(price: Double): Boolean = orderType match {
      case BuyOrder => price <= threshold
      case SellOrder => price >= threshold
    }
    override def decreasedBy(quantity: Double): StopOrder =
      StopOrder(orderType, orderQuantity - quantity, orderThreshold, orderId)
  }

}