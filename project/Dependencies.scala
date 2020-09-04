import sbt._

object Dependencies {
  lazy val protobufVersion = "3.13.0"
  lazy val protobuf =
    "com.google.protobuf" % "protobuf-java-util" % protobufVersion
  lazy val guava = "com.google.guava" % "guava" % "29.0-jre"
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.1"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "2.4.4"
  lazy val scallop = "org.rogach" %% "scallop" % "3.5.1"
  lazy val gcs = "com.google.cloud" % "google-cloud-storage" % "1.113.0"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.13.0"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.12"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.13.0"
}
