package darkpool.models

import java.util.UUID

import darkpool.engine.commands._
import darkpool.models.common.ThresholdQuantity
import darkpool.models.orders._
import spray.json._

/**
 * Created by: film42 on: 3/14/15.
 */

case class Error(error: String)

object TradingJsonProtocol extends DefaultJsonProtocol {

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    override def read(json: JsValue): UUID = {
      val toRemove = "\"".toSet
      val cleanUuidString = json.toString().filterNot(toRemove)
      UUID.fromString(cleanUuidString)
    }

    override def write(obj: UUID): JsValue = JsString(obj.toString)
  }

  implicit object OrderTypeFormat extends RootJsonFormat[OrderType] {
    override def read(json: JsValue): OrderType = json match {
      case JsString("buy") => BuyOrder
      case JsString("sell") => SellOrder
    }
    override def write(obj: OrderType): JsValue = obj match {
      case BuyOrder => JsString("buy")
      case SellOrder => JsString("sell")
    }
  }

  implicit object OrderFormat extends RootJsonFormat[Order] {
    override def read(json: JsValue): Order = {
      val hasThreshold = json.asJsObject.fields.contains("orderThreshold")
      if(hasThreshold) {
        json.convertTo[LimitOrder]
      } else {
        json.convertTo[MarketOrder]
      }
    }
    override def write(obj: Order): JsValue = obj match {
      case o: LimitOrder =>
        o.toJson
      case o: MarketOrder =>
        o.toJson
    }
  }

  implicit object OrderAddedFormat extends RootJsonFormat[OrderAdded.type] {
    override def read(json: JsValue): OrderAdded.type = ???
    override def write(obj: OrderAdded.type): JsValue = JsObject("status" -> JsString("Order Added"))
  }

  implicit object OrderNotAddedFormat extends RootJsonFormat[OrderNotAdded.type] {
    override def read(json: JsValue): OrderNotAdded.type = ???
    override def write(obj: OrderNotAdded.type): JsValue = JsObject("status" -> JsString("Order Not Addded"))
  }

  implicit object OrderCanceledFormat extends RootJsonFormat[OrderCanceled] {
    override def read(json: JsValue): OrderCanceled = ???
    override def write(obj: OrderCanceled): JsValue = {
      JsObject("status" -> JsString("Order Canceled"),
               "remainingOrder" -> obj.remainingOrder.toJson)
    }
  }

  implicit object OrderNotCanceledFormat extends RootJsonFormat[OrderNotCanceled.type] {
    override def read(json: JsValue): OrderNotCanceled.type = ???
    override def write(obj: OrderNotCanceled.type): JsValue = JsObject("status" -> JsString("Order Not Canceled"))
  }

  implicit val tradeJsonFormat = jsonFormat7(Trade)

  implicit val thresholdQuantityJsonFormat = jsonFormat2(ThresholdQuantity)
  implicit val marketSnapshotJsonFormat = jsonFormat4(MarketSnapshot)

  implicit val errorJsonFormat = jsonFormat1(Error)

  implicit val limitOrderJsonFormat = jsonFormat5(LimitOrder)
  implicit val marketOrderJsonFormat = jsonFormat4(MarketOrder)

  implicit val ordersForAccountFormat = jsonFormat1(OrdersForAccount)
  implicit val ordersForAccountResponseFormat = jsonFormat1(OrdersForAccountResponse)

}
