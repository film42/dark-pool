package darkpool.engine

import darkpool.book.OrderBook
import darkpool.models.orders.BuyOrder
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class MatchingEngineSelfMatchPreventionSpec extends FunSpec with Matchers with BeforeAndAfter {

  var orderBook = new OrderBook(BuyOrder)

  before {
    orderBook = new OrderBook(BuyOrder)
  }

  // We will use the Nasdaq self match prevention spec for tests: Version 1
  // Source: https://www.nasdaqtrader.com/content/productsservices/trading/selfmatchprevention.pdf

  describe("Self Trade Prevention") {

    it("will remove orders that cancel out if share size is the same") {

    }

    it("will cancel the smaller order and adjust the larger order") {

    }

  }
}
