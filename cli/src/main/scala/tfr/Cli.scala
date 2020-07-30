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
package tfr

import java.io.InputStream

import org.rogach.scallop._
import cats.Show
import io.circe.Encoder
import cats.effect.{ContextShift, IO, Resource}
import fs2._
import org.tensorflow.example.Example
import tensorflow.serving.PredictionLogOuterClass.PredictionLog
import tfr.instances.example.{given _, _}
import tfr.instances.prediction.{given _, _}
import tfr.instances.output.{given _, _}
import scala.collection.immutable.ArraySeq

object Cli {
  given ioContextShift as ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  final class Options(arguments: Seq[String]) extends ScallopConf(arguments) {
    printedName = "tfr"
    banner("""Usage: tfr [options] <files? | STDIN>
             |TensorFlow TFRecord reader CLI tool
             |Options:
             |""".stripMargin)

    val record: ScallopOption[String] =
      opt[String](
        default = Some("example"),
        descr = "Record type to be read { example | prediction_log }"
      )
    val checkCrc32 = opt[Boolean](
      default = Some(false),
      descr = "Enable checks CRC32 on each record"
    )
    val number = opt[Int](descr = "Number of records to output")
    val flat = opt[Boolean](
      default = Some(false),
      descr = "Output examples as flat JSON objects"
    )
    val files =
      trailArg[List[String]](required = false, descr = "files? | STDIN")

    verify()
  }

  def main(args: Array[String]): Unit = {
    val options = Options(ArraySeq.unsafeWrapArray(args))
    val resources = options.files() match {
      case Nil => Resources.stdin[IO] :: Nil
      case l   => l.iterator.map(Resources.file[IO]).toList
    }

    options.record() match {
      case "example" =>
        given exampleEncoder as Encoder[Example] = 
          if (options.flat()) {
            flat.exampleEncoder
          } else {
            tfr.instances.example.exampleEncoder
          }

        run[Example](options, resources)
      case "prediction_log" =>
        given predictionLogEncoder as Encoder[PredictionLog] =
          tfr.instances.prediction.predictionLogEncoder

        run[PredictionLog](options, resources)
    }
  }

  def run[T: Parsable: Show](
      options: Options,
      resources: List[Resource[IO, InputStream]]
  ): Unit = {
    val reader = TFRecord.resourceReader[IO, T](
      TFRecord.typedReader[T, IO](options.checkCrc32())
    )
    val records =
      Stream(resources.map(reader.run): _*).parJoin(resources.length)

    options.number
      .map(records.take(_))
      .getOrElse(records)
      .showLines(Console.out)
      .compile
      .drain
      .unsafeRunSync()
  }
}
