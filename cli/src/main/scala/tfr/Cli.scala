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

import java.io.FileInputStream

import caseapp._
import cats.effect.{IO, Resource}
import fs2._
import org.tensorflow.example.Example
import tfr.instances._

@AppName("tfr")
@ArgsName("files")
final case class Options(
    checkCrc32: Boolean = false,
    @ExtraName("n")
    number: Option[Int] = None
)

object Cli extends CaseApp[Options] {

  override def run(options: Options, args: RemainingArgs): Unit = {
    val resources = args.remaining match {
      case Nil =>
        Stream.resource(Resource.fromAutoCloseable(IO.delay(System.in)))
      case l =>
        l.iterator
          .map { path =>
            Stream.resource(Resource.fromAutoCloseable(IO.delay {
              new FileInputStream(new java.io.File(path))
            }))
          }
          .reduce(_ ++ _)
    }

    val exampleRecords = resources.flatMap(
      TFRecord
        .streamReader[IO, Example](
          TFRecord.readerAsExample[IO](options.checkCrc32)
        )
        .run
    )

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
      .unsafeRunAsync {
        case Left(value)  => Console.out.println(value.getMessage())
        case Right(value) => ()
      }
  }

}
