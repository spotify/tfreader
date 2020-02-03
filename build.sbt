/*
 * Copyright 2020 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.spotify"
ThisBuild / organizationName := "spotify"
ThisBuild / licenses := Seq(
  "APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / homepage := Some(url("https://github.com/spotify/tfreader"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/spotify/tfreader"),
    "scm:git@github.com:spotify/tfr.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "regadas",
    name = "Filipe Regadas",
    email = "filiperegadas@gmail.com",
    url = url("https://github.com/regadas")
  )
)
ThisBuild / publishMavenStyle := true
ThisBuild / pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)
ThisBuild / credentials += (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  username,
  password
))
ThisBuild / dynverSonatypeSnapshots := true
ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val tfr = project
  .in(file("."))
  .settings(publish / skip := true)
  .aggregate(core, cli)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "tfr-core",
    libraryDependencies ++= Seq(
      catsCore,
      fs2Io,
      guava,
      protobuf,
      scalaTest % Test,
      tensorFlowProto
    ),
    addCompilerPlugin(kindProjector)
  )

lazy val cli = project
  .in(file("cli"))
  .settings(
    name := "tfr-cli",
    libraryDependencies ++= Seq(
      caseApp,
      catsCore,
      fs2Io,
      scalaTest % Test,
      tensorFlowProto
    ),
    name in GraalVMNativeImage := "tfr",
    graalVMNativeImageOptions ++= Seq(
      "-H:+ReportExceptionStackTraces",
      "-H:ReflectionConfigurationFiles=" + baseDirectory.value / "src" / "graal" / "reflect-config.json",
      "--no-fallback",
      "--initialize-at-build-time"
    )
  )
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)
