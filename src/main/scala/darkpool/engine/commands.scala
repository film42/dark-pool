package darkpool.engine

import darkpool.models.common.ThresholdQuantity
import darkpool.models.orders.Order

package object commands {
  case class Add(order: Order)
  trait AddOrderResponse
  case object OrderAdded extends AddOrderResponse
  case object OrderNotAdded extends AddOrderResponse

  case class Cancel(order: Order)
  case class OrderCanceled(remainingOrder: Order)
  case object OrderNotCanceled

  case object Snapshot
  case class MarketSnapshot(spread: Double, buyBook: List[ThresholdQuantity], sellBook: List[ThresholdQuantity],
                            referencePrice: Double)
}
