package darkpool.models

import java.util.UUID

import spray.json._

/**
 * Created by: film42 on: 3/14/15.
 */

object TradingJsonProtocol extends DefaultJsonProtocol {

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    override def read(json: JsValue): UUID = UUID.fromString(json.toString())

    override def write(obj: UUID): JsValue = JsString(obj.toString)
  }

  implicit val tradeJsonFormat = jsonFormat6(Trade)

//  implicit object TradeJsonFormat extends RootJsonFormat[Trade] {
//    override def write(trade: Trade) = JsArray(
//      JsString(trade.buyerId.toString),
//      JsString(trade.sellerId.toString),
//      JsString(trade.buyOrderId.toString),
//      JsString(trade.sellOrderId.toString),
//      JsNumber(trade.price),
//      JsNumber(trade.quantity),
//      JsString(trade.createdAt.toString)
//    )
//
//    override def read(json: JsValue): Trade = ???
//  }

}
