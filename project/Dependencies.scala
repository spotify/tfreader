import sbt._

object Dependencies {
  lazy val protobufVersion = "3.17.3"
  lazy val protobuf =
    "com.google.protobuf" % "protobuf-java-util" % protobufVersion
  lazy val guava = "com.google.guava" % "guava" % "30.1.1-jre"
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.6.1"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "3.0.6"
  lazy val scallop = "org.rogach" %% "scallop" % "4.0.3"
  lazy val gcs = "com.google.cloud" % "google-cloud-storage" % "1.117.1"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.14.1"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.27"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.14.1"
}
