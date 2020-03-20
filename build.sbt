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
ThisBuild / scalacOptions ++= Seq(
  "-Ywarn-unused",
  "-target:11"
)
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

lazy val tfr = project
  .in(file("."))
  .settings(publish / skip := true)
  .aggregate(core, cli)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "tfr-core",
    libraryDependencies ++= Seq(
      gcs,
      catsCore,
      fs2Io,
      guava,
      protobuf,
      munit % Test,
      tensorFlowProto,
      circeCore
    ),
    testFrameworks += new TestFramework("munit.Framework"),
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
      tensorFlowProto
    ),
    name in GraalVMNativeImage := "tfr",
    graalVMNativeImageOptions ++= Seq(
      "-H:+ReportExceptionStackTraces",
      "-H:EnableURLProtocols=http,https",
      "-H:ReflectionConfigurationFiles=" + baseDirectory.value / "src" / "graal" / "reflect-config.json",
      "--no-fallback",
      "--initialize-at-build-time",
      "--allow-incomplete-classpath"
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("module-info.class") => MergeStrategy.rename
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)
