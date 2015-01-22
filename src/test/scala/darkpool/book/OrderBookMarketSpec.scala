package darkpool.book

import java.util.UUID

import darkpool.models.orders.{MarketOrder, LimitOrder, SellOrder, BuyOrder}
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class OrderBookMarketSpec extends FunSpec with Matchers with BeforeAndAfter {
  var orderBookBuy = new OrderBook(BuyOrder)
  var orderBookSell = new OrderBook(SellOrder)

  before {
    orderBookBuy = new OrderBook(BuyOrder)
    orderBookSell = new OrderBook(SellOrder)
  }

  describe("Price and time priority of market orders over limit orders") {
    it("buy book: market order has priority over a limit order") {
      val limitOrder = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID())
      orderBookBuy.add(limitOrder)
      orderBookBuy.orders shouldBe List(limitOrder)

      val marketOrder = MarketOrder(BuyOrder, 100, UUID.randomUUID())
      orderBookBuy.add(marketOrder)
      orderBookBuy.orders shouldBe List(marketOrder, limitOrder)
    }

    it("buy book: can correctly queue multiple market orders and limit orders") {
      val limitOrder = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID())
      orderBookBuy.add(limitOrder)
      orderBookBuy.orders shouldBe List(limitOrder)

      val marketOrder = MarketOrder(BuyOrder, 100, UUID.randomUUID())
      orderBookBuy.add(marketOrder)
      orderBookBuy.orders shouldBe List(marketOrder, limitOrder)

      val limitOrder2 = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID())
      orderBookBuy.add(limitOrder2)
      orderBookBuy.orders shouldBe List(marketOrder, limitOrder, limitOrder2)

      val marketOrder2 = MarketOrder(BuyOrder, 100, UUID.randomUUID())
      orderBookBuy.add(marketOrder2)
      orderBookBuy.orders shouldBe List(marketOrder, marketOrder2, limitOrder, limitOrder2)
    }

    it("sell book: market order has priority over a limit order") {
      val limitOrder = LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID())
      orderBookSell.add(limitOrder)
      orderBookSell.orders shouldBe List(limitOrder)

      val marketOrder = MarketOrder(SellOrder, 100, UUID.randomUUID())
      orderBookSell.add(marketOrder)
      orderBookSell.orders shouldBe List(marketOrder, limitOrder)
    }

    it("sell book: can correctly queue multiple market orders and limit orders") {
      val limitOrder = LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID())
      orderBookSell.add(limitOrder)
      orderBookSell.orders shouldBe List(limitOrder)

      val marketOrder = MarketOrder(SellOrder, 100, UUID.randomUUID())
      orderBookSell.add(marketOrder)
      orderBookSell.orders shouldBe List(marketOrder, limitOrder)

      val limitOrder2 = LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID())
      orderBookSell.add(limitOrder2)
      orderBookSell.orders shouldBe List(marketOrder, limitOrder, limitOrder2)

      val marketOrder2 = MarketOrder(SellOrder, 100, UUID.randomUUID())
      orderBookSell.add(marketOrder2)
      orderBookSell.orders shouldBe List(marketOrder, marketOrder2, limitOrder, limitOrder2)
    }
  }
}
