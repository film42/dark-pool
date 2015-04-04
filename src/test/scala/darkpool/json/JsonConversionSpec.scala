package darkpool.json

import java.util.UUID
import darkpool.models.orders._
import org.scalatest.{Matchers, FunSpec}

import darkpool.models.TradingJsonProtocol._
import spray.json._

class JsonConversionSpec extends FunSpec with Matchers {
  describe("MarketOrder") {
    it("can serialize and deserialize a market order") {
      val marketOrderJson = """{ "orderType": "sell", "orderQuantity": 22, "orderId": "1eb5576d-d983-4073-9574-0d10de9a657a", "accountId": "21bff678-1963-4a2b-9121-6b4e514504df" }"""

      val marketOrder = MarketOrder(
        SellOrder,
        22,
        UUID.fromString("1eb5576d-d983-4073-9574-0d10de9a657a"),
        UUID.fromString("21bff678-1963-4a2b-9121-6b4e514504df")
      )

      marketOrderJson.asJson.convertTo[MarketOrder] shouldBe marketOrder
      marketOrderJson.asJson.convertTo[Order] shouldBe marketOrder
    }
  }

  describe("LimitOrder") {
    it("can serialize and deserialize a limit order") {
      val limitOrderJson = """{ "orderType": "sell", "orderThreshold": 10.53, "orderQuantity": 22, "orderId": "1eb5576d-d983-4073-9574-0d10de9a657a", "accountId": "21bff678-1963-4a2b-9121-6b4e514504df" }"""

      val limitOrder = LimitOrder(
        SellOrder,
        22,
        10.53,
        UUID.fromString("1eb5576d-d983-4073-9574-0d10de9a657a"),
        UUID.fromString("21bff678-1963-4a2b-9121-6b4e514504df")
      )

      limitOrderJson.asJson.convertTo[LimitOrder] shouldBe limitOrder
      limitOrderJson.asJson.convertTo[Order] shouldBe limitOrder
    }
  }
}
