package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.models.orders.{LimitOrder, SellOrder, BuyOrder}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class MatchingEngineSelfMatchPreventionSpec extends FunSpec with Matchers with BeforeAndAfter {

  var orderBookBuy = new OrderBook(BuyOrder)
  var orderBookSell = new OrderBook(SellOrder)
  var matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)

  before {
    orderBookBuy = new OrderBook(BuyOrder)
    orderBookSell = new OrderBook(SellOrder)
    matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)
  }

  // We will use the Nasdaq self match prevention spec for tests: Version 1
  // Source: https://www.nasdaqtrader.com/content/productsservices/trading/selfmatchprevention.pdf

  describe("Self Trade Prevention") {

    it("will cancel the smaller order and adjust the larger order") {
      val accountId = UUID.randomUUID()

      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.6, UUID.randomUUID(), accountId))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 200, 10.5, UUID.randomUUID(), accountId))

      matchingEngine.trades shouldBe Nil

      // The buy order will dissolve
      matchingEngine.books(BuyOrder)._1.orders shouldBe Nil

      // The sell order will decrease by the buy order
      matchingEngine.books(SellOrder)._1.orders.head.quantity shouldBe 100
    }

    it("will remove orders that cancel out if share size is the same") {
      val accountId = UUID.randomUUID()

      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.6, UUID.randomUUID(), accountId))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID(), accountId))

      matchingEngine.trades shouldBe Nil

      // The buy order and sell order will dissolve
      matchingEngine.books(BuyOrder)._1.orders shouldBe Nil
      matchingEngine.books(SellOrder)._1.orders shouldBe Nil
    }

    it("will only check for self trade at execution time") {
      val accountId = UUID.randomUUID()

      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), accountId))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.6, UUID.randomUUID(), accountId))

      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.6, UUID.randomUUID(), UUID.randomUUID()))

      matchingEngine.trades.size shouldBe 2
      matchingEngine.books(BuyOrder)._1.orders shouldBe Nil
      matchingEngine.books(SellOrder)._1.orders shouldBe Nil
    }

  }
}
