package darkpool.models

import java.util.UUID

import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports._

object orders {
  //
  // Order Traits
  //
  trait CreatedAt {
    def created_at: DateTime = DateTime.now
  }

  trait ID {
    def id: UUID
  }

  trait Quantity {
    def quantity: Double
  }

  trait Threshold {
    def threshold: Double
  }

  trait AskOrder
  trait BidOrder

  trait BaseOrder extends Quantity with CreatedAt with ID

  trait MarketOrder extends BaseOrder
  trait LimitOrder extends BaseOrder with Threshold
  trait StopOrder extends BaseOrder with Threshold
  trait FillOrKillOrder extends BaseOrder with Threshold
  trait DayOrder extends BaseOrder with Threshold {
    def expires_at: DateTime
  }

  //
  // Order Type Implementations
  //
  sealed trait AskMarketOrder extends MarketOrder with AskOrder
  def AskMarketOrder(orderQuantity: Double, orderId: UUID): AskMarketOrder = {
    new AskMarketOrder {
      override def id: UUID = orderId
      override def quantity: Double = orderQuantity
    }
  }

  sealed trait BidMarketOrder extends MarketOrder with BidOrder
  def BidMarketOrder(orderQuantity: Double, orderId: UUID): BidMarketOrder = {
    new BidMarketOrder {
      override def id: UUID = orderId
      override def quantity: Double = orderQuantity
    }
  }

  sealed trait AskLimitOrder extends LimitOrder with AskOrder
  def AskLimitOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): AskLimitOrder = {
    new AskLimitOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
    }
  }

  sealed trait BidLimitOrder extends LimitOrder with BidOrder
  def BidLimitOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): BidLimitOrder = {
    new BidLimitOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
    }
  }

  sealed trait AskStopOrder extends StopOrder with AskOrder
  def AskStopOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): AskStopOrder = {
    new AskStopOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
    }
  }

  sealed trait BidStopOrder extends StopOrder with BidOrder
  def BidStopOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): BidStopOrder = {
    new BidStopOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
    }
  }

  sealed trait AskFillOrKillOrder extends FillOrKillOrder with AskOrder
  def AskFillOrKillOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): AskFillOrKillOrder = {
    new AskFillOrKillOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
    }
  }

  sealed trait BidFillOrKillOrder extends FillOrKillOrder with BidOrder
  def BidFillOrKillOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): BidFillOrKillOrder = {
    new BidFillOrKillOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
    }
  }

  sealed trait AskDayOrder extends DayOrder with AskOrder
  def AskDayOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): AskDayOrder = {
    new AskDayOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
      override def expires_at: Imports.DateTime = DateTime.tomorrow
    }
  }

  sealed trait BidDayOrder extends DayOrder with BidOrder
  def BidDayOrder(orderQuantity: Double, orderThreshold: Double, orderId: UUID): BidDayOrder = {
    new BidDayOrder {
      override def quantity: Double = orderQuantity
      override def threshold: Double = orderThreshold
      override def id: UUID = orderId
      override def expires_at: Imports.DateTime = DateTime.tomorrow
    }
  }
}