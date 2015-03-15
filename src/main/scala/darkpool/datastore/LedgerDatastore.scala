package darkpool.datastore

import java.util.UUID

import darkpool.models.Trade

/**
 * Created by: film42 on: 3/15/15.
 */
object LedgerDatastore {
  var trades = List[Trade]()

  def save(trade: Trade) {
    trades = trades :+ trade
  }

  def all: List[Trade] = trades

  def limit(count: Int): List[Trade] =
    trades.take(count)

  def find(tradeId: UUID): Option[Trade] =
    trades.find(_.tradeId == tradeId)

}
