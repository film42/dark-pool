package darkpool.models

import java.util.UUID

import darkpool.models.orders._
import com.github.nscala_time.time.Imports._
import org.scalatest.{Matchers, FunSpec}

class OrdersSpec extends FunSpec with Matchers {
  describe("AskLimitOrder") {
    it("can be created with correct params") {
      val randomUUID = UUID.randomUUID()
      val askLimitOrder = AskLimitOrder(3.5, 300.00, randomUUID)

      askLimitOrder.createdAt should be > (DateTime.now - 1.second)
      askLimitOrder.id shouldBe randomUUID
      askLimitOrder.threshold shouldBe 300.00
      askLimitOrder.quantity shouldBe 3.5
    }

    it("extends an AskOrder trait") {
      val askLimitOrder = AskLimitOrder(3.5, 300.00, UUID.randomUUID())
      assert(askLimitOrder.isInstanceOf[AskOrder])
    }
  }

  describe("BidMarketOrder") {
    it("can be created with correct params") {
      val randomUUID = UUID.randomUUID()
      val bidMarketOrder = BidMarketOrder(3.5, randomUUID)

      bidMarketOrder.createdAt should be > (DateTime.now - 1.second)
      bidMarketOrder.id shouldBe randomUUID
      bidMarketOrder.quantity shouldBe 3.5
    }

    it("extends an BidOrder trait") {
      val bidMarketOrder = BidMarketOrder(3.5, UUID.randomUUID())
      assert(bidMarketOrder.isInstanceOf[BidOrder])
    }
  }
}