organization := "io.github.Kvitral"
name := "scala-money-store"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "io.monix" %% "monix" % "3.0.0-RC2"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test

val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "de.heikoseeberger" % "akka-http-circe_2.12" % "1.22.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

enablePlugins(GatlingPlugin)
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.1.1" % "test,it"
libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.0.1.1" % "test,it"

lazy val main = (project in file(".")).settings(
  assemblyJarName := "scala-money-store.jar",
  mainClass := Some("com.kvitral.Server")
)
