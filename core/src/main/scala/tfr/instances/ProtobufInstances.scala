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

import java.util.Base64

import cats.Show
import com.google.protobuf.{ByteString, Message}
import com.google.protobuf.util.JsonFormat
import io.circe.Encoder

trait ProtobufInstances
    extends ProtobufShowInstances
    with ProtobufEncoderInstances

trait ProtobufShowInstances {
  given showProtobuf[A <: Message] as Show[A] {
      private[this] val Printer =
        JsonFormat.printer().omittingInsignificantWhitespace()

      override def show(t: A): String = Printer.print(t)
    }
}

trait ProtobufEncoderInstances {
  given byteStringEncoder as Encoder[ByteString] =
    Encoder.encodeString.contramap { s =>
      if s.isValidUtf8 then {
        s.toStringUtf8
      } else {
        Base64.getEncoder.encodeToString(s.toByteArray)
      }
    }
}
