package darkpool.engine

import java.util.UUID

import akka.actor.{Props, ActorSystem}
import darkpool.actors.{QueryActor, MatchingEngineActor}
import darkpool.book.OrderBook
import darkpool.models.orders.{LimitOrder, SellOrder, BuyOrder}
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}
import darkpool.engine.commands._

class MatchingEngineAkkaInterfaceSpec extends FunSpec with Matchers with BeforeAndAfter {

  describe("Use akka interface into matching engine") {
    val orderBookBuy = new OrderBook(BuyOrder)
    val orderBookSell = new OrderBook(SellOrder)

    it("should receive orders and generate a snapshot when asked") {
      val system = ActorSystem("darkpool-testing")
      val engine = system.actorOf(Props(new MatchingEngineActor(orderBookBuy, orderBookSell)), "engine")
      system.actorOf(Props[QueryActor], "api")

      engine ! Add(LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID(), UUID.randomUUID()))
      engine ! Add(LimitOrder(BuyOrder, 200, 10.4, UUID.randomUUID(), UUID.randomUUID()))
      engine ! Add(LimitOrder(BuyOrder, 50, 10.4, UUID.randomUUID(), UUID.randomUUID()))
      engine ! Add(LimitOrder(BuyOrder, 30, 10.4, UUID.randomUUID(), UUID.randomUUID()))

      engine ! Add(LimitOrder(SellOrder, 50, 11, UUID.randomUUID(), UUID.randomUUID()))
      engine ! Add(LimitOrder(SellOrder, 50, 10.2, UUID.randomUUID(), UUID.randomUUID()))

      Thread.sleep(1000)

      engine ! Snapshot
    }
  }
}
