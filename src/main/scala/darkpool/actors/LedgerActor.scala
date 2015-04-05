package darkpool.actors

import akka.actor.{ActorLogging, Actor}
import darkpool.models.Trade

class LedgerActor extends Actor with ActorLogging {
  import darkpool.datastore.LedgerDatastore._

  def recordTransaction(trade: Trade) {
    trade match {
      case Trade(id, buyer, seller, buyOrder, sellOrder, price, quantity) =>
        log.info(s"$buyer bought $quantity from $seller for $$$price")

        // Call to in memory datastore
        save(trade)
    }
  }

  override def receive: Receive = {
    case trade @ Trade(_, _, _, _, _, _, _) =>
      recordTransaction(trade)
  }
}
