import sbt._

object Dependencies {
  lazy val protobuf = "com.google.protobuf" % "protobuf-java-util" % "3.11.1"
  lazy val tensorFlowProto = "org.tensorflow" % "proto" % "1.15.0"
  lazy val guava = "com.google.guava" % "guava" % "28.2-jre"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.0"
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.0"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "2.2.1"
  lazy val caseApp = "com.github.alexarchambault" %% "case-app" % "2.0.0-M10"
  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
}
