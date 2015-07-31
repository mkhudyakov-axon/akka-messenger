package reactive.api

import reactive.chat.ChatService
import reactive.websocket.WebSocketServer
import akka.actor.{ ActorSystem, Props }
import akka.event.Logging.InfoLevel
import spray.http.{ HttpRequest, StatusCodes }
import spray.routing.{ Directives, RouteConcatenation }
import spray.routing.directives.LogEntry

trait AbstractSystem {
  implicit def system : ActorSystem
}

trait ReactiveApi extends RouteConcatenation with StaticRoute with AbstractSystem {
  this : MainActors =>
  private def showReq(req : HttpRequest) = LogEntry(req.uri, InfoLevel)

  val rootService = system.actorOf(Props(new RootService[BasicRouteActor](routes)), "routes")
  lazy val routes = logRequest(showReq _) {
    new ChatService(chat).route ~
    staticRoute
  }
  val wsService = system.actorOf(Props(new RootService[WebSocketServer](wsroutes)), "wss")
  lazy val wsroutes = logRequest(showReq _) {
    new ChatService(chat).wsroute ~
    complete(StatusCodes.NotFound)
  }
}

trait StaticRoute extends Directives {
  this : AbstractSystem =>

  lazy val staticRoute =
    pathEndOrSingleSlash {
      getFromResource("index.html")
    } ~ complete(StatusCodes.NotFound)
}
