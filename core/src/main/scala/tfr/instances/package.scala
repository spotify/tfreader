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

import cats.Show
import io.circe.{Printer => CircePrinter}
import io.circe.syntax._
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.tensorflow.example.Example

package object instances {

  def showProtoAsJson[A <: Message]: Show[A] = new Show[A] {
    private[this] val Printer =
      JsonFormat.printer().omittingInsignificantWhitespace()

    override def show(t: A): String = Printer.print(t)
  }

  def showExampleAsFlattenedJson: Show[Example] = new Show[Example] {
    private[this] val Printer =
      CircePrinter.noSpaces.copy(dropNullValues = true)

    override def show(t: Example): String =
      Printer.print(t.asJson(coders.exampleEncoder))
  }

}
