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

ThisBuild / scalaVersion := "0.27.0-RC1"
ThisBuild / scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-Ykind-projector"
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
  .in(file("modules/core"))
  .settings(
    name := "tfr-core",
    compileOrder := CompileOrder.JavaThenScala,
    libraryDependencies ++= Seq(gcs, guava, munit % Test),
    libraryDependencies ++= Seq(
      catsCore,
      fs2Io,
      circeCore,
      circeParser
    ).map(_.withDottyCompat(scalaVersion.value)),
    testFrameworks += new TestFramework("munit.Framework"),
    version in ProtobufConfig := protobufVersion
  )
  .enablePlugins(ProtobufPlugin)

lazy val cli = project
  .in(file("modules/cli"))
  .settings(
    name := "tfr-cli",
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "tfr",
    libraryDependencies ++= Seq(catsCore, fs2Io, scallop).map(
      _.withDottyCompat(scalaVersion.value)
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
  .enablePlugins(BuildInfoPlugin)
