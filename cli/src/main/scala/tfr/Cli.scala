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
import cats.effect.{ContextShift, IO}
import fs2._
import org.tensorflow.example.Example
import tensorflow.serving.PredictionLogOuterClass.PredictionLog
import tfr.TFRecord.ReadError
import tfr.instances.example._
import tfr.instances.prediction._
import tfr.instances.output._



@AppName("tfr")
@ArgsName("files? | STDIN")
final case class Options(
    @ExtraName("n")
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
      case Nil =>
        Stream.resource(Resources.stdin[IO]) :: Nil
      case l =>
        l.iterator.map { path =>
          Stream.resource(Resources.file[IO](path))
        }.toList
    }

    options.record match {
      case "example" => printExample(options, resources)
      case "prediction_log" => printPredictionLog(options, resources)
    }
  }

  def printExample(options: Options, resources: List[Stream[IO, InputStream]]): Unit = {
    implicit val exampleEncoder: Encoder[Example] = if (options.flat) {
      flat.exampleEncoder
    } else {
      tfr.instances.example.exampleEncoder
    }

    val examples = resources.map { resource =>
      resource.flatMap(
        TFRecord
          .streamReader[IO, Example](
            TFRecord.typedReader[Example, IO](options.checkCrc32)
          )
          .run
      )
    }
    print(options,  Stream(examples: _*).parJoin(examples.length))
  }

  def printPredictionLog(options: Options, resources: List[Stream[IO, InputStream]]): Unit = {
    implicit val predictionLogEncoder: Encoder[PredictionLog] =
      tfr.instances.prediction.predictionLogEncoder

    val predictionLogs = resources.map { resource =>
      resource.flatMap(
        TFRecord
          .streamReader[IO, PredictionLog](
            TFRecord.typedReader[PredictionLog, IO](options.checkCrc32)
          )
          .run
      )
    }
    print(options,  Stream(predictionLogs: _*).parJoin(predictionLogs.length))
  }

  def print[T: Show](options: Options, records: Stream[IO, Either[ReadError, T]]): Unit = {
    options.number
      .map(records.take(_))
      .getOrElse(records)
      .showLines(Console.out)
      .compile
      .drain
      .unsafeRunSync()
  }

}
