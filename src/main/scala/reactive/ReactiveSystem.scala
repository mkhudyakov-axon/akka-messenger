package reactive

import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import reactive.api.{ MainActors, ReactiveApi, ReactiveSecurityConfig }
import akka.actor.{Props, ActorSystem, PoisonPill}
import akka.io.IO
import spray.can.Http
import spray.can.server.UHttp

object ReactiveSystem extends App with MainActors with ReactiveApi with ReactiveSecurityConfig {

  val configResource = System.getProperty("config.resource") match {
    case x: String => x
    case _  =>
      System.setProperty("config.resource", "application.conf")
      "application.conf"
  }

  val config = ConfigFactory.load(configResource,
    ConfigParseOptions.defaults().setAllowMissing(false),
    ConfigResolveOptions.defaults())  
  
  implicit lazy val system = ActorSystem("chat-system", config)
  
  /* Init Journal */
  val ref = system.actorOf(Props[SharedLeveldbStore], "store")
  SharedLeveldbJournal.setStore(ref, system)

  IO(UHttp) ! Http.Bind(wsService, Configuration.host, Configuration.portWs)
  /* Since the UHttp extension extends from Http extension, it starts an actor whose name will later collide with the Http extension. */
  system.actorSelection("/user/IO-HTTP") ! PoisonPill
  
  /* We could use IO(UHttp) here instead of killing the "/user/IO-HTTP" actor */
  IO(Http) ! Http.Bind(rootService, Configuration.host, Configuration.portHttp)

  sys.addShutdownHook({ IO(UHttp) ! Http.Unbind; system.shutdown() })
}

object Configuration {
  import com.typesafe.config.ConfigFactory
 
  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  lazy val host = config.getString("messenger.host")
  lazy val portHttp = config.getInt("messenger.ports.http")
  lazy val portWs = config.getInt("messenger.ports.ws")
}
