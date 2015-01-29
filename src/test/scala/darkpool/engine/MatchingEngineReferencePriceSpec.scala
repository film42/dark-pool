package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.models.orders.{MarketOrder, LimitOrder, SellOrder, BuyOrder}
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class MatchingEngineReferencePriceSpec extends FunSpec with Matchers with BeforeAndAfter {

  var orderBookBuy = new OrderBook(BuyOrder)
  var orderBookSell = new OrderBook(SellOrder)
  var matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)

  before {
    matchingEngine.referencePrice = 10.0
  }

  describe("Updating Reference Price From Open") {
    it("maintains reference price as trades occur") {
      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 11, UUID.randomUUID(), UUID.randomUUID()))
      matchingEngine.acceptOrder(LimitOrder(SellOrder, 100, 11, UUID.randomUUID(), UUID.randomUUID()))

      matchingEngine.trades.size shouldBe 1
      matchingEngine.referencePrice shouldBe 11

      matchingEngine.acceptOrder(LimitOrder(BuyOrder, 100, 12, UUID.randomUUID(), UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID(), UUID.randomUUID()))

      matchingEngine.trades.size shouldBe 2
      matchingEngine.referencePrice shouldBe 12

      matchingEngine.acceptOrder(MarketOrder(BuyOrder, 100, UUID.randomUUID(), UUID.randomUUID()))
      matchingEngine.acceptOrder(MarketOrder(SellOrder, 100, UUID.randomUUID(), UUID.randomUUID()))

      matchingEngine.trades.size shouldBe 3
      matchingEngine.referencePrice shouldBe 12
    }
  }

}
