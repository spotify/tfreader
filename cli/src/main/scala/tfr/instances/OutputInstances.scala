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
import org.tensorflow.example.Example

trait OutputInstances {
  implicit def eitherShow(
      implicit es: Show[Example],
      ts: Show[Throwable]
  ): Show[Either[Throwable, Example]] =
    new Show[Either[Throwable, Example]] {
      override def show(t: Either[Throwable, Example]): String =
        t.fold(ts.show, es.show)
    }

  implicit val throwableShow: Show[Throwable] = new Show[Throwable] {
    override def show(t: Throwable): String = t.getMessage
  }
}
