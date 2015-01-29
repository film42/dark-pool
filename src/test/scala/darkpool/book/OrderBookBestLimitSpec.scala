package darkpool.book

import java.util.UUID

import darkpool.models.orders.{LimitOrder, BuyOrder, MarketOrder, SellOrder}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class OrderBookBestLimitSpec extends FunSpec with Matchers with BeforeAndAfter {
  var orderBookBuy = new OrderBook(BuyOrder)
  var orderBookSell = new OrderBook(SellOrder)

  before {
    orderBookBuy = new OrderBook(BuyOrder)
    orderBookSell = new OrderBook(SellOrder)
  }

  describe("Best limit with limit and market orders") {
    it("the best limit for Buy/ Sell order book is None") {
      orderBookBuy.bestLimit shouldBe None
      orderBookSell.bestLimit shouldBe None
    }

    it("does not have a best limit order given only market orders") {
      orderBookBuy.add(MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID()))
      orderBookSell.add(MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID()))

      orderBookBuy.bestLimit shouldBe None
      orderBookSell.bestLimit shouldBe None
    }

    it("various life cycles of the order book best limit for BUY") {
      val marketOrder = MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.add(marketOrder)
      val firstOrder = LimitOrder(BuyOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.add(firstOrder)

      orderBookBuy.bestLimit.get shouldBe firstOrder.threshold
      orderBookBuy.orders shouldBe List(marketOrder, firstOrder)

      val conservativeOrder = LimitOrder(BuyOrder, 100, 10.4, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.add(conservativeOrder)
      orderBookBuy.bestLimit.get shouldBe firstOrder.threshold
      orderBookBuy.orders shouldBe List(marketOrder, firstOrder, conservativeOrder)

      val aggressiveOrder = LimitOrder(BuyOrder, 100, 10.6, UUID.randomUUID(), UUID.randomUUID())
      orderBookBuy.add(aggressiveOrder)
      orderBookBuy.bestLimit.get shouldBe aggressiveOrder.threshold
      orderBookBuy.orders shouldBe List(marketOrder, aggressiveOrder, firstOrder, conservativeOrder)

      orderBookBuy.decreaseTopBy(100)
      orderBookBuy.orders shouldBe List(aggressiveOrder, firstOrder, conservativeOrder)

      orderBookBuy.decreaseTopBy(100)
      orderBookBuy.orders shouldBe List(firstOrder, conservativeOrder)

      orderBookBuy.decreaseTopBy(100)
      orderBookBuy.orders shouldBe List(conservativeOrder)

      orderBookBuy.decreaseTopBy(100)
      orderBookBuy.top shouldBe None
    }

    it("various life cycles of the order book best limit for SELL") {
      val marketOrder = MarketOrder(SellOrder, 100, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.add(marketOrder)
      val firstOrder = LimitOrder(SellOrder, 100, 10.5, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.add(firstOrder)

      orderBookSell.bestLimit.get shouldBe firstOrder.threshold
      orderBookSell.orders shouldBe List(marketOrder, firstOrder)

      val conservativeOrder = LimitOrder(SellOrder, 100, 10.6, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.add(conservativeOrder)
      orderBookSell.bestLimit.get shouldBe firstOrder.threshold
      orderBookSell.orders shouldBe List(marketOrder, firstOrder, conservativeOrder)

      val aggressiveOrder = LimitOrder(SellOrder, 100, 10.4, UUID.randomUUID(), UUID.randomUUID())
      orderBookSell.add(aggressiveOrder)
      orderBookSell.bestLimit.get shouldBe aggressiveOrder.threshold
      orderBookSell.orders shouldBe List(marketOrder, aggressiveOrder, firstOrder, conservativeOrder)

      orderBookSell.decreaseTopBy(100)
      orderBookSell.orders shouldBe List(aggressiveOrder, firstOrder, conservativeOrder)

      orderBookSell.decreaseTopBy(100)
      orderBookSell.orders shouldBe List(firstOrder, conservativeOrder)

      orderBookSell.decreaseTopBy(100)
      orderBookSell.orders shouldBe List(conservativeOrder)

      orderBookSell.decreaseTopBy(100)
      orderBookSell.top shouldBe None
    }
  }
}
