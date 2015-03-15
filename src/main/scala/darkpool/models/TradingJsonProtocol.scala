package darkpool.models

import java.util.UUID

import darkpool.engine.commands.MarketSnapshot
import darkpool.models.common.ThresholdQuantity
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

  implicit val thresholdQuantityFormat = jsonFormat2(ThresholdQuantity)
  implicit val marketSnapshotFormat = jsonFormat4(MarketSnapshot)

}
