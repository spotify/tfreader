import sbt._

object Dependencies {
  lazy val protobufVersion = "4.27.2"
  lazy val protobuf =
    "com.google.protobuf" % "protobuf-java-util" % protobufVersion
  lazy val guava = "com.google.guava" % "guava" % "33.2.1-jre"
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.12.0"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "3.10.2"
  lazy val scallop = "org.rogach" %% "scallop" % "5.1.0"
  lazy val gcs = "com.google.cloud" % "google-cloud-storage" % "2.40.0"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.14.7"
  lazy val munit = "org.scalameta" %% "munit" % "1.0.0"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.14.7"
}
