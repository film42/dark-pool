package darkpool.engine

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import darkpool.actors.{LedgerActor, MatchingEngineActor, QueryActor}
import darkpool.book.OrderBook
import darkpool.engine.commands._
import darkpool.models.orders.{BuyOrder, LimitOrder, MarketOrder, SellOrder}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpecLike, Matchers}

import scala.concurrent.duration._

class MatchingEngineAkkaInterfaceSpec
  extends TestKit(ActorSystem("MatchingEngineAkkaInterfaceSpec"))
  with DefaultTimeout with ImplicitSender
  with FunSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val orderBookBuy = new OrderBook(BuyOrder)
  val orderBookSell = new OrderBook(SellOrder)
  val engineActor = system.actorOf(Props(new MatchingEngineActor(orderBookBuy, orderBookSell)), "engineActor")
  val apiActor = system.actorOf(Props[QueryActor], "api")
  val ledgerActor = system.actorOf(Props[LedgerActor], "ledger")

  describe("Use akka interface into matching engine") {
    it("should receive orders and generate a snapshot when asked") {
      within(1 second) {
        engineActor ! Add(LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)
        engineActor ! Add(LimitOrder(BuyOrder, 200, 10.4, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)
        engineActor ! Add(LimitOrder(BuyOrder, 50, 10.4, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)
        engineActor ! Add(MarketOrder(BuyOrder, 30, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)

        engineActor ! Add(LimitOrder(SellOrder, 50, 11, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)
        engineActor ! Add(LimitOrder(SellOrder, 50, 10.2, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)
        engineActor ! Add(MarketOrder(SellOrder, 50, UUID.randomUUID(), UUID.randomUUID()))
        expectMsg(OrderAdded)

        engineActor ! Snapshot
        expectMsgClass(MarketSnapshot(0, Nil, Nil, 0).getClass)
      }
    }
  }

  override def afterAll() {
    shutdown()
  }
}
