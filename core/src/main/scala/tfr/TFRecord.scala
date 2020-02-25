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
import fs2.{Pull, Stream}
import org.tensorflow.example.Example
import tfr.TFExample.parser

object TFRecord {
  sealed abstract class ReadError
  case object EmptyHeader extends ReadError
  case object InvalidCrc32 extends ReadError

  private[this] val HeaderLength: Int =
    (java.lang.Long.SIZE + java.lang.Integer.SIZE) / java.lang.Byte.SIZE
  private[this] val FooterLength
      : Int = java.lang.Integer.SIZE / java.lang.Byte.SIZE
  private[this] val Crc32c = Hashing.crc32c()

  private def mask(crc: Int): Int = ((crc >>> 15) | (crc << 17)) + 0xa282ead8
  private def hashLong(x: Long): Int = mask(Crc32c.hashLong(x).asInt())
  private def hashBytes(x: Array[Byte]): Int = mask(Crc32c.hashBytes(x).asInt())

  private def reader_(
      checkCrc32: Boolean
  ): InputStream => Either[ReadError, Array[Byte]] =
    input => {
      val headerBytes = read(input, HeaderLength)
      if (headerBytes.isEmpty) {
        Left(EmptyHeader)
      } else {
        val headerBuf =
          ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
        val length = headerBuf.getLong
        if (checkCrc32 && hashLong(length) != headerBuf.getInt) {
          Left(InvalidCrc32)
        } else {
          val data = read(input, length.toInt)
          val footerBytes = read(input, FooterLength)
          if (checkCrc32 && hashBytes(data) != ByteBuffer
                .wrap(footerBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt) {
            Left(InvalidCrc32)
          }
          Right(data)
        }
      }
    }

  def reader[F[_]: Sync](
      checkCrc32: Boolean
  ): Kleisli[F, InputStream, Either[ReadError, Array[Byte]]] =
    Kleisli(input => Sync[F].delay(reader_(checkCrc32).apply(input)))

  def readerAsExample[F[_]: Sync](
      checkCrc32: Boolean
  ): Kleisli[F, InputStream, Either[ReadError, Example]] = {
    TFRecord.reader[F](checkCrc32).andThen { elem =>
      elem match {
        case Left(value) =>
          Sync[F].delay(Left(value): Either[ReadError, Example])
        case Right(value) =>
          parser
            .andThen(ex => Sync[F].delay(Right(ex): Either[ReadError, Example]))
            .run(value)
      }
    }
  }

  def streamReader[F[_]: Sync, A](
      reader: Kleisli[F, InputStream, Either[ReadError, A]]
  ): Kleisli[Stream[F, *], InputStream, Either[ReadError, A]] = Kleisli {
    input =>
      Stream
        .repeatEval(reader.apply(input))
        .repeatPull(_.uncons1.flatMap {
          case None                         => Pull.pure(None)
          case Some((Left(EmptyHeader), _)) => Pull.pure(None)
          case Some((elem, stream))         => Pull.output1(elem).as(Some(stream))
        })
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
