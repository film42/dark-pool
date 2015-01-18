package darkpool.models

import java.util.UUID

import darkpool.models.common._

package object orders {
  //
  // Order Traits
  //
  trait OrderType

  trait AskOrder extends OrderType
  trait BidOrder extends OrderType

  trait Order extends Quantity with CreatedAt with ID

  trait Threshold {
    def threshold: Double
  }

  trait MarketOrder extends Order
  trait LimitOrder extends Order with Threshold
  trait StopOrder extends Order with Threshold

  //
  // Order Type Implementations
  //
  case class AskMarketOrder(orderQuantity: Double, orderId: UUID) extends MarketOrder with AskOrder {
    override def id: UUID = orderId
    override def quantity: Double = orderQuantity
  }

  case class BidMarketOrder(orderQuantity: Double, orderId: UUID) extends MarketOrder with BidOrder {
    override def id: UUID = orderId
    override def quantity: Double = orderQuantity
  }

  case class AskLimitOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID) extends LimitOrder with AskOrder {
    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
  }

  case class BidLimitOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID) extends LimitOrder with BidOrder {
    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
  }

  case class AskStopOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID) extends StopOrder with AskOrder {
    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
  }

  case class BidStopOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID) extends StopOrder with BidOrder {
    override def quantity: Double = orderQuantity
    override def threshold: Double = orderThreshold
    override def id: UUID = orderId
  }
}