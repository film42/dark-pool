package darkpool.book

import java.util.UUID

import darkpool.models.orders._
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class OrderBookLimitOrdersSpec extends FunSpec with Matchers with BeforeAndAfter {
  var orderBookBuy = new OrderBook(BuyOrder)
  var orderBookSell = new OrderBook(SellOrder)

  before {
    orderBookBuy = new OrderBook(BuyOrder)
    orderBookSell = new OrderBook(SellOrder)
  }

  describe("Price and time priority of limit orders") {
    it("can add a single limit order to the BUY order book") {
      val order = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.addOrder(order)
      orderBookBuy.orders.size shouldBe 1
      orderBookBuy.top.get shouldBe order
    }

    it("can add two limit orders to the BUY order book, with more aggressive order first") {
      val order1 = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      val order2 = LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.addOrder(order1)
      orderBookBuy.addOrder(order2)
      orderBookBuy.orders shouldBe List(order1, order2)
    }

    it("can add two limit orders to the BUY order book, with less aggressive order first") {
      val order1 = LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID(), UUID.randomUUID())
      val order2 = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.addOrder(order1)
      orderBookBuy.addOrder(order2)
      orderBookBuy.orders shouldBe List(order2, order1)
    }

    it("can add two limit orders to the BUY order book, with the same price limit") {
      val order1 = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      val order2 = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.addOrder(order1)
      orderBookBuy.addOrder(order2)
      orderBookBuy.orders shouldBe List(order1, order2)
    }

    it("can add a single limit order to the SELL order book") {
      val order = LimitOrder(SellOrder, 100, 10.6, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.addOrder(order)
      orderBookSell.orders.size shouldBe 1
      orderBookSell.top.get shouldBe order
    }

    it("can add two limit orders to the SELL order book, with more aggressive order first") {
      val order1 = LimitOrder(SellOrder, 100, 10.6, UUID.randomUUID(), UUID.randomUUID())
      val order2 = LimitOrder(SellOrder, 100, 10.7, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.addOrder(order1)
      orderBookSell.addOrder(order2)
      orderBookSell.orders shouldBe List(order1, order2)
    }

    it("can add two limit orders to the SELL order book, with the same price limit") {
      val order1 = LimitOrder(SellOrder, 100, 10.7, UUID.randomUUID(), UUID.randomUUID())
      val order2 = LimitOrder(SellOrder, 100, 10.7, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.addOrder(order1)
      orderBookSell.addOrder(order2)
      orderBookSell.orders shouldBe List(order1, order2)
    }

    it("can decrease top outstanding order partially and then fill it completely") {
      val order1 = LimitOrder(BuyOrder, 100, 10000.7, UUID.randomUUID(), UUID.randomUUID())
      val order2 = LimitOrder(BuyOrder, 100, 10000.7, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.addOrder(order1)
      orderBookBuy.addOrder(order2)

      orderBookBuy.decreaseTopBy(20)
      orderBookBuy.orders.size shouldBe 2
      orderBookBuy.top.get.quantity shouldBe 80

      orderBookBuy.decreaseTopBy(80)
      orderBookBuy.orders.size shouldBe 1
      orderBookBuy.top.get.quantity shouldBe 100
    }
  }
}
