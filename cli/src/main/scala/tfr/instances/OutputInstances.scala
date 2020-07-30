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
import tfr.TFRecord.{EmptyHeader, InvalidCrc32, ReadError}

trait OutputInstances {
  given eitherReadErrorShow[T](using
      es: Show[T],
      ts: Show[ReadError]
  ) as Show[Either[ReadError, T]] {
      override def show(t: Either[ReadError, T]): String =
        t.fold(ts.show, es.show)
    }

  given errorShow as Show[ReadError] {
    override def show(t: ReadError): String =
      t match {
        case EmptyHeader  => "empty header"
        case InvalidCrc32 => "invalid crc32"
        case ReadError    => "unexpected read error"
      }
  }
}
