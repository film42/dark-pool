package darkpool.engine

import java.util.UUID

import darkpool.book.OrderBook
import darkpool.models.orders.{BuyOrder, LimitOrder, SellOrder}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class MatchingEngineCancelOrdersSpec extends FunSpec with Matchers with BeforeAndAfter {

  describe("Canceling Orders from Matching Engine") {

    var orderBookBuy = new OrderBook(BuyOrder)
    var orderBookSell = new OrderBook(SellOrder)
    var matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)

    before {
      // TODO: Add flush methods
      orderBookBuy = new OrderBook(BuyOrder)
      orderBookSell = new OrderBook(SellOrder)
      matchingEngine = new MatchingEngine(orderBookBuy, orderBookSell)
    }

    it("will delete an order than has not been fulfilled") {
      val buyOrder = LimitOrder(BuyOrder, 200, 10.5, UUID.randomUUID(), UUID.randomUUID())
      val sellOrder = LimitOrder(SellOrder, 200, 100.5, UUID.randomUUID(), UUID.randomUUID())

      matchingEngine.acceptOrder(buyOrder)
      matchingEngine.acceptOrder(sellOrder)

      orderBookBuy.top.get shouldEqual buyOrder
      orderBookSell.top.get shouldEqual sellOrder

      matchingEngine.cancelOrder(buyOrder)
      matchingEngine.cancelOrder(sellOrder)

      orderBookBuy.top shouldEqual None
      orderBookSell.top shouldEqual None
    }

  }

}
