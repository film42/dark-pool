package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.models.orders._
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class MatchingEngineLimitOrderSpec extends FunSpec with Matchers with BeforeAndAfter {

  describe("Limit Order Scenarios") {

    var orderBookBuy = new OrderBook(BuyOrder)
    var orderBookSell = new OrderBook(SellOrder)
    var matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)

    before {
      // TODO: Add flush methods
      orderBookBuy = new OrderBook(BuyOrder)
      orderBookSell = new OrderBook(SellOrder)
      matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)

      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 200, 10.3, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.7, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 200, 10.8, UUID.randomUUID()))
    }

    it("can match identical buy order to outstanding sell order") {
      val buyOrder = LimitOrder(BuyOrder, 100, 10.7, UUID.randomUUID())
      matchingEngine.acceptOrder(buyOrder)

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.7
      trade.quantity shouldBe 100
      trade.buyerUUID shouldBe buyOrder.id
    }

    it("can match trade price higher than top of book") {
      val buyOrder = LimitOrder(BuyOrder, 100, 10.8, UUID.randomUUID())
      matchingEngine.acceptOrder(buyOrder)

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.7
      trade.quantity shouldBe 100
      trade.buyerUUID shouldBe buyOrder.id
    }

    it("matches a Sell order large enough to clear the Buy book") {
      val limitOrder = LimitOrder(SellOrder, 350, 10.3, UUID.randomUUID())
      matchingEngine.acceptOrder(limitOrder)

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.4
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.3
      trades(1).quantity shouldBe 200

      orderBookSell.orders.size shouldBe 3
      orderBookSell.top.get.quantity shouldBe 50
    }

    it("matches a large Buy order partially") {
      val limitOrder = LimitOrder(BuyOrder, 350, 10.7, UUID.randomUUID())
      matchingEngine.acceptOrder(limitOrder)

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.7
      trade.quantity shouldBe 100

      orderBookBuy.top.get.quantity shouldBe 250
      orderBookBuy.top.get.id shouldBe limitOrder.id
      orderBookSell.top.get.quantity shouldBe 200
    }

    it("matches a large sell order partially") {
      val limitOrder = LimitOrder(SellOrder, 350, 10.4, UUID.randomUUID())
      matchingEngine.acceptOrder(limitOrder)

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.4
      trade.quantity shouldBe 100

      orderBookSell.top.get.quantity shouldBe 250
      orderBookSell.top.get.id shouldBe limitOrder.id
      orderBookBuy.top.get.quantity shouldBe 200
    }
  }

}
