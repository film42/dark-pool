package darkpool.engine

import java.util.UUID

import darkpool.models.common.ThresholdQuantity
import darkpool.models.orders.Order

package object commands {
  case class OrdersForAccount(accountId: UUID)
  case class OrdersForAccountResponse(orders: List[Order])

  case class Add(order: Order)
  trait AddOrderResponse
  case object OrderAdded extends AddOrderResponse
  case object OrderNotAdded extends AddOrderResponse

  case class Cancel(order: Order)
  trait CancelOrderResponse
  case class OrderCanceled(remainingOrder: Order) extends CancelOrderResponse
  case object OrderNotCanceled extends CancelOrderResponse

  case object Snapshot
  case class MarketSnapshot(spread: Double, buyBook: List[ThresholdQuantity], sellBook: List[ThresholdQuantity],
                            referencePrice: Double)
}
