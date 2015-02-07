package darkpool.book

import java.util.UUID

import darkpool.models.orders.{BuyOrder, LimitOrder, MarketOrder}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class OrderBookCancelSpec extends FunSpec with Matchers with BeforeAndAfter {
  var orderBook = new OrderBook(BuyOrder)

  before {
    orderBook = new OrderBook(BuyOrder)
  }

  describe("Canceling an Order") {
    it("cant find an order that doesnt exist") {
      val limitOrder = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      val marketOrder = MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID())
      orderBook.cancelOrder(limitOrder)
      orderBook.cancelOrder(marketOrder)
      orderBook.orders shouldBe Nil
      orderBook.canceledOrders shouldBe Nil

      orderBook.addOrder(LimitOrder(BuyOrder, 20, 10.5, UUID.randomUUID(), UUID.randomUUID()))

      orderBook.cancelOrder(limitOrder)
      orderBook.cancelOrder(marketOrder)
      orderBook.orders.size shouldBe 1
      orderBook.canceledOrders shouldBe Nil
    }

    it("can cancel an unfulfilled limit order") {
      val order = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order)
      orderBook.orders shouldBe List(order)

      orderBook.cancelOrder(order)
      orderBook.orders shouldBe Nil
      orderBook.canceledOrders shouldBe List(order)
    }

    it("can cancel an unfulfilled limit order when multiple limit orders at threshold exist") {
      val order = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order)
      val order2 = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order2)
      orderBook.orders shouldBe List(order, order2)

      orderBook.cancelOrder(order)
      orderBook.orders shouldBe List(order2)
      orderBook.canceledOrders shouldBe List(order)
    }

    it("can cancel an unfulfilled limit order when multiple limit orders at different threshold exist") {
      val order = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order)
      val order2 = LimitOrder(BuyOrder, 50, 10.2, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order2)
      orderBook.orders shouldBe List(order, order2)

      orderBook.cancelOrder(order)
      orderBook.orders shouldBe List(order2)
      orderBook.canceledOrders shouldBe List(order)
    }

    it("can cancel an unfulfilled market order") {
      val order = MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order)
      orderBook.orders shouldBe List(order)

      orderBook.cancelOrder(order)
      orderBook.orders shouldBe Nil
      orderBook.canceledOrders shouldBe List(order)
    }

    it("can cancel an unfulfilled market order when multiple market orders exist") {
      val order = MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order)
      val order2 = MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(order2)

      orderBook.orders shouldBe List(order, order2)

      orderBook.cancelOrder(order)
      orderBook.orders shouldBe List(order2)
      orderBook.canceledOrders shouldBe List(order)
    }

    it("can cancel a partially matched order") {
      val limitOrder = LimitOrder(BuyOrder, 140, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(limitOrder)
      orderBook.decreaseTopBy(100)
      orderBook.cancelOrder(limitOrder)

      orderBook.canceledOrders.head.quantity shouldBe 40
      orderBook.canceledOrders.head.id shouldBe limitOrder.id
      orderBook.top shouldBe None
    }

    it("can cancel a partially matched order with multiple orders") {
      val limitOrder = LimitOrder(BuyOrder, 140, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBook.addOrder(limitOrder)
      orderBook.addOrder(LimitOrder(BuyOrder, 123, 10.2, UUID.randomUUID(), UUID.randomUUID()))
      orderBook.decreaseTopBy(100)
      orderBook.cancelOrder(limitOrder)

      orderBook.canceledOrders.head.quantity shouldBe 40
      orderBook.canceledOrders.head.id shouldBe limitOrder.id
      orderBook.top.get.quantity shouldBe 123
    }

  }
}
