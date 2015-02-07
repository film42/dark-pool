package darkpool.actors

import akka.actor.{Actor, ActorLogging}
import darkpool.engine.commands.MarketSnapshot

class QueryActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case marketSnapshot @ MarketSnapshot(_, _, _, _) =>
      log.info(s"Received market snapshot: $marketSnapshot")
  }
}
