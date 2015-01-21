package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.models.orders._
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class MatchingEngineSpec extends FunSpec with Matchers with BeforeAndAfter {

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
  }

}
