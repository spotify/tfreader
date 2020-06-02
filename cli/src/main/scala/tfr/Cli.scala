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

import caseapp._
import cats.Show
import io.circe.Encoder
import cats.effect.{ContextShift, IO, Resource}
import fs2._
import org.tensorflow.example.Example
import tensorflow.serving.PredictionLogOuterClass.PredictionLog
import tfr.instances.example._
import tfr.instances.prediction._
import tfr.instances.output._

@AppName("tfr")
@ArgsName("files? | STDIN")
final case class Options(
    @ExtraName("r")
    @HelpMessage("What type of record should be read")
    record: String = "example",
    @HelpMessage("If enabled checks CRC32 on each record")
    checkCrc32: Boolean = false,
    @ExtraName("n")
    @HelpMessage("Number of records to output")
    number: Option[Int] = None,
    @ExtraName("f")
    @HelpMessage("Output examples as flat JSON objects")
    flat: Boolean = false
)

object Cli extends CaseApp[Options] {
  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  override def run(options: Options, args: RemainingArgs): Unit = {

    val resources = args.remaining match {
      case Nil => Resources.stdin[IO] :: Nil
      case l   => l.iterator.map(Resources.file[IO]).toList
    }

    options.record match {
      case "example" =>
        implicit val exampleEncoder: Encoder[Example] = if (options.flat) {
          flat.exampleEncoder
        } else {
          tfr.instances.example.exampleEncoder
        }

        run[Example](options, resources)
      case "prediction_log" =>
        implicit val predictionLogEncoder: Encoder[PredictionLog] =
          tfr.instances.prediction.predictionLogEncoder

        run[PredictionLog](options, resources)
    }
  }

  def run[T: Parsable: Show](
      options: Options,
      resources: List[Resource[IO, InputStream]]
  ): Unit = {
    val reader = TFRecord.resourceReader[IO, T](
      TFRecord.typedReader[T, IO](options.checkCrc32)
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
