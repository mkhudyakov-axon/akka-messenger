package reactive.chat

import akka.actor.{ActorSystem, ActorRef}
import reactive.Configuration
import reactive.websocket.WebSocket
import spray.http.StatusCodes
import spray.routing.Directives

/**
 * @author Maksym Khudiakov
 */
class ChatService(find : ActorRef)(implicit system : ActorSystem) extends Directives {
  lazy val route =
    pathPrefix("chat") {
      val dir = "chat/"
      pathEndOrSingleSlash {
        getFromResource(dir + "index.html")
      } ~
      getFromResourceDirectory(dir)
    }
  lazy val wsroute =
    pathPrefix("chat") {
      path("ws" / Segment) { chatId =>
        implicit ctx =>
          ctx.responder ! WebSocket.Register(ctx.request, find, true)
      }
    }
}
