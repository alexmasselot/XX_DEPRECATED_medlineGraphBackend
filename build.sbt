name := "medline-graph-backend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.reactivemongo" %% "reactivemongo" % "0.11.5",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.5.play24",
  "com.google.maps" % "google-maps-services" % "0.1.7",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "org.apache.spark" %% "spark-core" % "1.4.1" % "provided",
  "org.apache.spark" %% "spark-sql" % "1.4.1",
  "org.apache.spark"  %% "spark-streaming" % "1.4.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

