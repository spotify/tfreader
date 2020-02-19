import sbt._

object Dependencies {
  lazy val protobuf = "com.google.protobuf" % "protobuf-java-util" % "3.11.4"
  lazy val tensorFlowProto = "org.tensorflow" % "proto" % "1.15.0"
  lazy val guava = "com.google.guava" % "guava" % "28.2-jre"
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.0"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "2.2.1"
  lazy val caseApp = "com.github.alexarchambault" %% "case-app" % "2.0.0-M10"
  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
  lazy val gcs = "com.google.cloud" % "google-cloud-storage" % "1.103.1"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.13.0-RC1"
  lazy val munit = "org.scalameta" %% "munit" % "0.4.5"
}
