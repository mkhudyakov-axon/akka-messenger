package reactive.chat

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import reactive.chat.MonitoringActor.{Unregister, Register}
import reactive.websocket.WebSocket

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * @author Maksym Khudiakov
 */
object ChatActor {
  
  type ChatId = String

  sealed trait AbstractMessage
  case class Unregister(ws: WebSocket) extends AbstractMessage
  case class Message(chatId: String, cmd: String, data: String)

  val idExtractor: ShardRegion.IdExtractor = {
    case req @ WebSocket.Open(ws)  => (ws.path(), req)
    case req @ WebSocket.Close(ws, code, reason) => (ws.path(), req)
    case req @ WebSocket.Error(ws, ex) => (ws.path(), req)
    case req @ WebSocket.Message(ws, msg) => (ws.path(), req)
    case req @ ChatActor.Unregister(ws) => (ws.path(), req)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case WebSocket.Open(ws)  => (math.abs(ws.path().hashCode) % 100).toString
    case WebSocket.Close(ws, code, reason) => (math.abs(ws.path().hashCode) % 100).toString
    case WebSocket.Error(ws, ex) => (math.abs(ws.path().hashCode) % 100).toString
    case WebSocket.Message(ws, msg) => (math.abs(ws.path().hashCode) % 100).toString
    case ChatActor.Unregister(ws) => (math.abs(ws.path().hashCode) % 100).toString
  }

  val shardName: String = "MessagingSystem"
}

class ChatActor(monitoring: ActorRef) extends Actor with ActorLogging {

  val participants = mutable.ListBuffer[WebSocket]()
  val messages = mutable.Map[ChatActor.ChatId, String]("1" -> "hello", "2" -> "hello there")

  /* Tell to the monitoring system, that I'm up */
  monitoring ! Register("ChatActor")
  
  context.setReceiveTimeout(2.minutes)

  override def receive = {
    case WebSocket.Open(ws) =>
      if (ws != null) {
        participants += ws
        /* Get chat id */
        for (msgEntry <- messages)
          ws.send(s"Received message '${msgEntry._2}' from ${msgEntry._1}")
      }

    case WebSocket.Close(ws, code, reason) =>
      self ! ChatActor.Unregister(ws)

    case WebSocket.Error(ws, ex) =>
      self ! ChatActor.Unregister(ws)

    case WebSocket.Message(ws, msg) =>
      if (ws != null) {
        val parts = ws.path().split("/")
        val chatId = parts(parts.length - 1)
        
        /* Add message to the chat */
        messages += chatId -> msg
        
        /* Notify users */
        participants.foreach { ws =>
          ws.send(s"Just received '$msg' from $chatId")
        }
      }

    case ChatActor.Unregister(ws) =>
      if (ws != null) {
        participants -= ws
      }
      
    case ReceiveTimeout =>
      monitoring ! Unregister("ChatActor")
      participants.foreach( ws => ws.close())
      context.parent ! Passivate(stopMessage = PoisonPill)
  }
}
