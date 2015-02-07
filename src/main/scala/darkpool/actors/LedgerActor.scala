package darkpool.actors

import akka.actor.{ActorLogging, Actor}
import darkpool.models.Trade

class LedgerActor extends Actor with ActorLogging {
  def recordTransaction(trade: Trade) {
    trade match {
      case Trade(buyer, seller, buyOrder, sellOrder, price, quantity) =>
        log.info(s"$buyer bought $quantity from $seller for $$$price")
    }
  }

  override def receive: Receive = {
    case trade @ Trade(_, _, _, _, _, _) =>
      recordTransaction(trade)
  }
}
