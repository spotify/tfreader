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

import caseapp._
import cats.effect.{ContextShift, IO}
import fs2._
import org.tensorflow.example.Example
import tfr.instances.example._

@AppName("tfr")
@ArgsName("files? | STDIN")
final case class Options(
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
    implicit val exampleEncoder = if (options.flat) {
      flat.exampleEncoder
    } else {
      tfr.instances.example.exampleEncoder
    }

    val resources = args.remaining match {
      case Nil =>
        Stream.resource(Resources.stdin[IO]) :: Nil
      case l =>
        l.iterator.map { path =>
          Stream.resource(Resources.file[IO](path))
        }.toList
    }

    val streams = resources.map { resource =>
      resource.flatMap(
        TFRecord
          .streamReader[IO, Example](
            TFRecord.readerAsExample[IO](options.checkCrc32)
          )
          .run
      )
    }

    val exampleRecords = Stream(streams: _*).parJoin(streams.length)

    options.number
      .map(exampleRecords.take(_))
      .getOrElse(exampleRecords)
      .collect {
        // we should display errors somehow
        case Right(example) => example
      }
      .showLines(Console.out)
      .compile
      .drain
      .unsafeRunSync()
  }

}
