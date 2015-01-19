package darkpool.models

import java.util.UUID

import darkpool.models.orders._
import com.github.nscala_time.time.Imports._
import org.scalatest.{Matchers, FunSpec}

class OrdersSpec extends FunSpec with Matchers {
  describe("LimitOrder") {
    it("can be created with correct params") {
      val randomUUID = UUID.randomUUID()
      val limitOrder = LimitOrder(3.5, 300.00, randomUUID)

      limitOrder.createdAt should be > (DateTime.now - 1.second)
      limitOrder.id shouldBe randomUUID
      limitOrder.threshold shouldBe 300.00
      limitOrder.quantity shouldBe 3.5
    }

    it("extends an Order trait") {
      val limitOrder = LimitOrder(3.5, 300.00, UUID.randomUUID())
      assert(limitOrder.isInstanceOf[Order])
    }
  }

  describe("MarketOrder") {
    it("can be created with correct params") {
      val randomUUID = UUID.randomUUID()
      val marketOrder = MarketOrder(3.5, randomUUID)

      marketOrder.createdAt should be > (DateTime.now - 1.second)
      marketOrder.id shouldBe randomUUID
      marketOrder.quantity shouldBe 3.5
    }

    it("extends an Order trait") {
      val marketOrder = MarketOrder(3.5, UUID.randomUUID())
      assert(marketOrder.isInstanceOf[Order])
    }
  }
}