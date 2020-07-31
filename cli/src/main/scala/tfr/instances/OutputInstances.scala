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
import tfr.TFRecord.Error

trait OutputInstances:
  given eitherReadErrorShow[T](using
      es: Show[T],
      ts: Show[Error]
  ) as Show[Either[Error, T]]:
      override def show(t: Either[Error, T]): String =
        t.fold(ts.show, es.show)

  given errorShow as Show[Error]:
    override def show(t: Error): String =
      t match
        case Error.EmptyHeader  => "empty header"
        case Error.InvalidCrc32 => "invalid crc32"
        case Error.ReadError    => "unexpected read error"
