package reactive.chat

import akka.actor.{ActorLogging, Actor}
import akka.contrib.pattern.ShardRegion
import reactive.chat.MonitoringActor.{Unregister, Register}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object MonitoringActor {
  
  case class Register(actorName: String)
  case class Unregister(actorName: String)

  case object Tick

  val idExtractor: ShardRegion.IdExtractor = {
    case req @ Register(actorName)  => (actorName, req)
    case req @ Unregister(actorName)  => (actorName, req)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case Register(actorName)  => (math.abs(actorName.hashCode) % 100).toString
    case Unregister(actorName)  => (math.abs(actorName.hashCode) % 100).toString
  }

  val shardName: String = "MonitoringSystem"
}

/**
 * @author Maksym Khudiakov
 */
class MonitoringActor extends Actor with ActorLogging {
  
  import MonitoringActor.Tick

  val countTask = context.system.scheduler.schedule(5.seconds, 5.seconds, self, Tick)
  var counters = Map[String, Int]()
  
  override def receive: Receive = {
    case Tick =>
      log.info(s"Active actors in a cluster: $counters")
      
    case Register(actorName) =>
      counters = counters.updated(actorName, counters.getOrElse(actorName, 0) + 1)

    case Unregister(actorName) =>
      counters = counters.updated(actorName, counters.getOrElse(actorName, 0) - 1)
  }
}
