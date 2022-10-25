import sbt._

object Dependencies {
  lazy val protobufVersion = "3.21.8"
  lazy val protobuf =
    "com.google.protobuf" % "protobuf-java-util" % protobufVersion
  lazy val guava = "com.google.guava" % "guava" % "31.1-jre"
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.8.0"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "3.3.0"
  lazy val scallop = "org.rogach" %% "scallop" % "4.1.0"
  lazy val gcs = "com.google.cloud" % "google-cloud-storage" % "2.5.1"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.14.3"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.14.3"
}
