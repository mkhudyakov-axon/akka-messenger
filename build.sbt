organization  := "cua.li"

version       := "0.5"

scalaVersion  := "2.10.4"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= {
  val akkaV  = "2.3.9"
  val sprayV = "1.3.3"
  val kamonV = "0.3.5"
  Seq(
    "com.typesafe.akka"   %%  "akka-actor"                      % akkaV             withSources() withJavadoc(),
    "com.typesafe.akka"   %%  "akka-slf4j"                      % akkaV             withSources() withJavadoc(),
    "com.typesafe.akka"   %%  "akka-cluster"                    % akkaV             withSources() withJavadoc(),
    "com.typesafe.akka"   %%  "akka-contrib"                    % akkaV             intransitive() withSources() withJavadoc(),
    "com.typesafe.akka"   %%  "akka-persistence-experimental"   % akkaV             intransitive() withSources() withJavadoc(),
    "org.iq80.leveldb"    %   "leveldb"                         % "0.7",
    "com.wandoulabs.akka" %%  "spray-websocket"                 % "0.1.4"           intransitive() withSources() withJavadoc(),
    "io.spray"            %%  "spray-json"                      % "1.3.1"           withSources() withJavadoc(),
    "io.spray"            %%  "spray-can"                       % sprayV            withSources() withJavadoc(),
    "io.spray"            %%  "spray-routing"                   % sprayV            withSources() withJavadoc(),
    "ch.qos.logback"      %   "logback-classic"                 % "1.1.3"
  )
}

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

scalacOptions in Test ++= Seq("-Yrangepos")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

doc in Compile <<= target.map(_ / "none")

publishArtifact in (Compile, packageSrc) := false

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

Revolver.settings

fork in run := true