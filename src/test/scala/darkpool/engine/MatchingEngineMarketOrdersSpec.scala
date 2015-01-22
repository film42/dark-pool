package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.models.orders.{MarketOrder, LimitOrder, BuyOrder, SellOrder}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class MatchingEngineMarketOrdersSpec extends FunSpec with Matchers with BeforeAndAfter {
  var orderBookBuy = new OrderBook(BuyOrder)
  var orderBookSell = new OrderBook(SellOrder)
  var matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)

  before {
    // TODO: Add flush methods
    orderBookBuy = new OrderBook(BuyOrder)
    orderBookSell = new OrderBook(SellOrder)
    matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)
    matchingEngine.referencePrice = 10.00
  }

  describe("Incoming Market Order Scenarios") {

    it("matches a large Buy market order against multiple limit orders") {
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.7, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 200, 10.6, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 300, 10.5, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 650, UUID.randomUUID()))

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.7
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.6
      trades(1).quantity shouldBe 200
      trades(2).price shouldBe 10.5
      trades(2).quantity shouldBe 300

      orderBookSell.top.get.quantity shouldBe 50
      orderBookSell.orders.size shouldBe 1
      orderBookBuy.orders.size shouldBe 0
    }

    it("matches a small Buy market order against limit orders") {
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.7, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 200, 10.6, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 300, 10.5, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 150, UUID.randomUUID()))

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.7
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.6
      trades(1).quantity shouldBe 50

      orderBookBuy.top.get.quantity shouldBe 150
      orderBookBuy.orders.size shouldBe 2
      orderBookSell.orders.size shouldBe 0
    }

    it("matches a large Sell market order against multiple limit orders") {
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 200, 10.6, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 300, 10.7, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 650, UUID.randomUUID()))

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.5
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.6
      trades(1).quantity shouldBe 200
      trades(2).price shouldBe 10.7
      trades(2).quantity shouldBe 300

      orderBookBuy.top.get.quantity shouldBe 50
      orderBookBuy.orders.size shouldBe 1
      orderBookSell.orders.size shouldBe 0
    }

    it("matches a small Sell market order against limit orders") {
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 200, 10.6, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 300, 10.7, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 150, UUID.randomUUID()))

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.5
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.6
      trades(1).quantity shouldBe 50

      orderBookSell.top.get.quantity shouldBe 150
      orderBookSell.orders.size shouldBe 2
      orderBookBuy.orders.size shouldBe 0
    }
  }

  describe("Incoming Limit Orders to Outstanding Market Order Scenarios") {
    it("matches incoming Buy limit order against a single outstanding Sell market order") {
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 120, 10.5, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.5
      trade.quantity shouldBe 100

      orderBookBuy.top.get.quantity shouldBe 20
      orderBookBuy.orders.size shouldBe 1
      orderBookSell.orders.size shouldBe 0
    }

    it("matches incoming Sell limit order against a single outstanding Buy market order") {
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 120, 10.5, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.5
      trade.quantity shouldBe 100

      orderBookSell.top.get.quantity shouldBe 20
      orderBookSell.orders.size shouldBe 1
      orderBookBuy.orders.size shouldBe 0
    }

    it("matches incoming Buy limit order against Sell market order while another NON-CROSSING Sell limit order is outstanding") {
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.6, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 120, 10.5, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.5
      trade.quantity shouldBe 100

      orderBookBuy.top.get.quantity shouldBe 20
      orderBookBuy.orders.size shouldBe 1
      orderBookSell.orders.size shouldBe 1
    }

    it("matches incoming Sell limit order against Buy market order while another NON-CROSSING Buy limit order is outstanding") {
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 120, 10.5, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.5
      trade.quantity shouldBe 100

      orderBookSell.top.get.quantity shouldBe 20
      orderBookSell.orders.size shouldBe 1
      orderBookBuy.orders.size shouldBe 1
    }

    it("matches incoming Buy limit order against Sell market order while another CROSSING Sell limit order is outstanding") {
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.4, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 120, 10.5, UUID.randomUUID()))

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.4
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.4
      trades(1).quantity shouldBe 20

      orderBookSell.top.get.quantity shouldBe 80
      orderBookSell.orders.size shouldBe 1
      orderBookBuy.orders.size shouldBe 0
    }

    it("matches incoming Sell limit order against Buy market order while another CROSSING Buy limit order is outstanding") {
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.6, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 120, 10.5, UUID.randomUUID()))

      val trades = matchingEngine.trades
      trades(0).price shouldBe 10.6
      trades(0).quantity shouldBe 100
      trades(1).price shouldBe 10.6
      trades(1).quantity shouldBe 20

      orderBookBuy.top.get.quantity shouldBe 80
      orderBookBuy.orders.size shouldBe 1
      orderBookSell.orders.size shouldBe 0
    }

    it("matches incoming Buy market order against Sell market order when another - limit - Sell order present") {
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.5
      trade.quantity shouldBe 100

      orderBookSell.top.get.quantity shouldBe 100
      orderBookSell.orders.size shouldBe 1
      orderBookBuy.orders.size shouldBe 0
    }

    it("matches incoming Sell market order against Buy market order when another - limit - Buy order present") {
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10.5
      trade.quantity shouldBe 100

      orderBookBuy.top.get.quantity shouldBe 100
      orderBookBuy.orders.size shouldBe 1
      orderBookSell.orders.size shouldBe 0
    }
  }


  describe("Matching Market Orders Using a Reference Price") {

    it("matches incoming Buy market order against Sell market order when no best limit price is available") {
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10
      trade.quantity shouldBe 100

      orderBookBuy.orders.size shouldBe 0
      orderBookSell.orders.size shouldBe 0
    }

    it("matches incoming Sell market order against Sell market order when no best limit price is available") {
      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID()))

      val trade = matchingEngine.trades.head
      trade.price shouldBe 10
      trade.quantity shouldBe 100

      orderBookBuy.orders.size shouldBe 0
      orderBookSell.orders.size shouldBe 0
    }
  }
}
