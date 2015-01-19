package darkpool.models

import java.util.UUID

import darkpool.models.common.{Quantity, CreatedAt}

case class Trade(buyerUUID: UUID, sellerUUID: UUID, price: Double, quantity: Double) extends CreatedAt with Quantity