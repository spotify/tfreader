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
import tfr.TFRecord.{EmptyHeader, InvalidCrc32, ReadError}

trait OutputInstances {
  implicit def eitherReadErrorExampleShow(implicit
      es: Show[Example],
      ts: Show[ReadError]
  ): Show[Either[ReadError, Example]] =
    new Show[Either[ReadError, Example]] {
      override def show(t: Either[ReadError, Example]): String =
        t.fold(ts.show, es.show)
    }

  implicit val errorShow: Show[ReadError] = new Show[ReadError] {
    override def show(t: ReadError): String =
      t match {
        case EmptyHeader  => "empty header"
        case InvalidCrc32 => "invalid crc32"
        case ReadError    => "unexpected read error"
      }
  }
}
