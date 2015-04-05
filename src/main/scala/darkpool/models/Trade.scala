package darkpool.models

import java.util.UUID

import darkpool.models.common.{Quantity, CreatedAt}

case class Trade(tradeId: UUID, buyerId: UUID, sellerId: UUID, buyOrderId: UUID, sellOrderId: UUID,
                 price: Double, quantity: Double) extends CreatedAt with Quantity