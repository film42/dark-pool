package darkpool.engine

import darkpool.models.common.ThresholdQuantity
import darkpool.models.orders.Order

package object commands {
  case class Add(order: Order)
  case class Cancel(order: Order)
  case class MarketSnapshot(spread: Double, buyBook: List[ThresholdQuantity], sellOrder: List[ThresholdQuantity],
                            referencePrice: Double)
  case object Snapshot
}
