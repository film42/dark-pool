package darkpool.engine

import java.util.UUID

import akka.actor.{Props, ActorSystem}
import darkpool.actors.LedgerActor
import darkpool.models.Trade
import darkpool.models.book.OrderBook
import darkpool.models.orders._

class MatchingEngine(buyOrder: OrderBook[BuyOrder], sellOrder: OrderBook[SellOrder]) {
  val system = ActorSystem("dark-pool")
  val ledger = system.actorOf(Props[LedgerActor])

  def insert[O <: Order](order: O): O = {
    println(order)
    order
  }

  def tryMatch(order: Order): Option[Order] = {

    ledger ! Trade(UUID.randomUUID(), UUID.randomUUID(), 10, 10)

    Some(order)
  }

  private def tryMatchWithTop(order: Order, top: Order): Option[Trade] = ???

}


