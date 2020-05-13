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

import cats.data.Kleisli
import cats.effect.Sync
import org.tensorflow.example.Example
import tensorflow.serving.PredictionLogOuterClass.PredictionLog

trait Parsable[T] {

  def parser[F[_]: Sync]: Kleisli[F, Array[Byte], T]

}

object Parsable {

  implicit val TFExampleParsable: Parsable[Example] = new Parsable[Example] {
    override def parser[F[_]: Sync]: Kleisli[F, Array[Byte], Example] =
      Kleisli(a => Sync[F].delay(Example.parseFrom(a)))
  }

  implicit val TFPredictionLogParsable: Parsable[PredictionLog] =
    new Parsable[PredictionLog] {
      override def parser[F[_]: Sync]: Kleisli[F, Array[Byte], PredictionLog] =
        Kleisli(a => Sync[F].delay(PredictionLog.parseFrom(a)))
    }
}
