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
package tfr.instances

import cats.Show
import com.google.protobuf.util.JsonFormat
import io.circe.{Encoder, Printer => CircePrinter}
import tensorflow.serving.PredictionLogOuterClass.PredictionLog

trait PredictionInstances
    extends PredictionLogShowInstances
    with PredictionLogEncoderInstances

trait PredictionLogShowInstances {
  private[this] val Printer =
    CircePrinter.noSpaces.copy(dropNullValues = true)

  given showPredictionLog(using
      encoder: Encoder[PredictionLog]
  ): Show[PredictionLog] with {
      override def show(t: PredictionLog): String =
        Printer.print(Encoder[PredictionLog].apply(t))
    }
}

trait PredictionLogEncoderInstances extends ProtobufEncoderInstances {
  import io.circe._
  import io.circe.parser._

  private[this] val ProtoPrinter =
    JsonFormat.printer().omittingInsignificantWhitespace()

  given predictionLogEncoder: Encoder[PredictionLog] with {
      override def apply(a: PredictionLog): Json =
        parse(ProtoPrinter.print(a)).getOrElse(Json.fromString(""))
    }
}
