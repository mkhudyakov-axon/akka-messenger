package reactive.api

import akka.contrib.pattern.ClusterSharding
import reactive.chat.{MonitoringActor, ChatActor}

import akka.actor.Props

trait MainActors {
  this : AbstractSystem =>

  val monitoring = ClusterSharding(system).start(
    typeName = MonitoringActor.shardName,
    entryProps = Some(Props[MonitoringActor]),
    idExtractor = MonitoringActor.idExtractor,
    shardResolver = MonitoringActor.shardResolver)
  
  val chat = ClusterSharding(system).start(
  typeName = ChatActor.shardName,
  entryProps = Some(Props.create(classOf[ChatActor], monitoring)),
  idExtractor = ChatActor.idExtractor,
  shardResolver = ChatActor.shardResolver)
}
