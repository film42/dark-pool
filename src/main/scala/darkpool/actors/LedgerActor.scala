package darkpool.actors

import akka.actor.Actor
import darkpool.models.Trade

class LedgerActor extends Actor {
  def recordTransaction(trade: Trade) {
    trade match {
      case Trade(buyer, seller, price, quantity) =>
        println(s"$buyer bought $quantity from $seller for $$$price")
    }
  }

  override def receive: Receive = {
    case trade @ Trade(_, _, _, _) =>
      recordTransaction(trade)
  }
}
