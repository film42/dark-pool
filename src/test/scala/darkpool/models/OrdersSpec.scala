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

      askLimitOrder.created_at should be > (DateTime.now - 1.second)
      askLimitOrder.id shouldBe randomUUID
      askLimitOrder.threshold shouldBe 300.00
      askLimitOrder.quantity shouldBe 3.5
    }

    it("extends an AskOrder trait") {
      val askLimitOrder = AskLimitOrder(3.5, 300.00, UUID.randomUUID())
      assert(askLimitOrder.isInstanceOf[AskOrder])
    }
  }

  describe("BidDayOrder") {
    it("can be created with correct params") {
      val bidDayOrder = BidDayOrder(3.5, 300.00, UUID.randomUUID())
      bidDayOrder.expires_at.hourOfDay shouldBe 24.hours.from(DateTime.now).hourOfDay()
      bidDayOrder.expires_at.dayOfMonth shouldBe (DateTime.now + 1.day).dayOfMonth
    }

    it("can match a BidOrder trait") {
      val bidDayOrder = BidDayOrder(3.5, 300.00, UUID.randomUUID())
      assert(bidDayOrder.isInstanceOf[BidOrder])
    }
  }
}
