package darkpool.engine

import darkpool.models.common.ThresholdQuantity
import darkpool.models.orders.Order

package object commands {
  case class Add(order: Order)
  case object OrderAdded
  case object OrderNotAdded

  case class Cancel(order: Order)
  case class OrderCanceled(remainingOrder: Order)
  case object OrderNotCanceled

  case object Snapshot
  case class MarketSnapshot(spread: Double, buyBook: List[ThresholdQuantity], sellOrder: List[ThresholdQuantity],
                            referencePrice: Double)
}
