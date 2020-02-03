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

import java.io.InputStream
import java.nio.{ByteBuffer, ByteOrder}

import cats.data.Kleisli
import cats.effect.Sync
import com.google.common.hash.Hashing
import fs2.Stream
import org.tensorflow.example.Example
import tfr.TFExample.parser

object TFRecord {
  private[this] val HeaderLength: Int =
    (java.lang.Long.SIZE + java.lang.Integer.SIZE) / java.lang.Byte.SIZE
  private[this] val FooterLength
      : Int = java.lang.Integer.SIZE / java.lang.Byte.SIZE
  private[this] val Crc32c = Hashing.crc32c()

  private def mask(crc: Int): Int = ((crc >>> 15) | (crc << 17)) + 0xa282ead8
  private def hashLong(x: Long): Int = mask(Crc32c.hashLong(x).asInt())
  private def hashBytes(x: Array[Byte]): Int = mask(Crc32c.hashBytes(x).asInt())

  private def reader_(checkCrc32: Boolean): InputStream => Array[Byte] =
    input => {
      val headerBytes = read(input, HeaderLength)
      require(headerBytes.nonEmpty, "could not read record header")
      val headerBuf =
        ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
      val length = headerBuf.getLong
      require(
        !checkCrc32 || hashLong(length) == headerBuf.getInt,
        "Invalid masked CRC32 of length"
      )

      val data = read(input, length.toInt)

      val footerBytes = read(input, FooterLength)
      require(
        !checkCrc32 || hashBytes(data) == ByteBuffer
          .wrap(footerBytes)
          .order(ByteOrder.LITTLE_ENDIAN)
          .getInt,
        "Invalid masked CRC32 of data"
      )
      data
    }

  def reader[F[_]: Sync](
      checkCrc32: Boolean
  ): Kleisli[F, InputStream, Array[Byte]] =
    Kleisli(input => Sync[F].delay(reader_(checkCrc32).apply(input)))

  def readerAsExample[F[_]: Sync](
      checkCrc32: Boolean
  ): Kleisli[F, InputStream, Example] =
    TFRecord.reader[F](checkCrc32).andThen(parser)

  def streamReader[F[_]: Sync, A](
      reader: Kleisli[F, InputStream, A]
  ): Kleisli[Stream[F, *], InputStream, Either[Throwable, A]] = Kleisli {
    input =>
      Stream.repeatEval(reader.apply(input)).attempt
  }

  private def read(input: InputStream, length: Int): Array[Byte] = {
    val data = Array.ofDim[Byte](length)
    var n = 0
    var off = 0
    do {
      n = input.read(data, off, data.length - off)
      if (n > 0) {
        off += n
      }
    } while (n > 0 && off < data.length)
    if (n <= 0) Array.emptyByteArray else data
  }

}
