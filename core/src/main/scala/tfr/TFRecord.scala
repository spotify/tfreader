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

import scala.util.Try

import cats.data.Kleisli
import cats.effect.Sync
import com.google.common.hash.Hashing
import fs2.{Pull, Stream}
import cats.effect.Resource

object TFRecord {
  sealed abstract class ReadError
  case object EmptyHeader extends ReadError
  case object InvalidCrc32 extends ReadError
  case object ReadError extends ReadError

  private[this] val HeaderLength: Int =
    (java.lang.Long.SIZE + java.lang.Integer.SIZE) / java.lang.Byte.SIZE
  private[this] val FooterLength: Int =
    java.lang.Integer.SIZE / java.lang.Byte.SIZE
  private[this] val Crc32c = Hashing.crc32c()

  private def mask(crc: Int): Int = ((crc >>> 15) | (crc << 17)) + 0xa282ead8
  private def hashLong(x: Long): Int = mask(Crc32c.hashLong(x).asInt())
  private def hashBytes(x: Array[Byte]): Int = mask(Crc32c.hashBytes(x).asInt())

  private def reader_(
      checkCrc32: Boolean
  ): InputStream => Either[ReadError, Array[Byte]] =
    input => {
      read(input, HeaderLength)
        .filterOrElse(_.nonEmpty, EmptyHeader)
        .map { headerBytes =>
          val buffer =
            ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
          (buffer.getLong, buffer.getInt)
        }
        .filterOrElse(
          header => !checkCrc32 || hashLong(header._1) == header._2,
          InvalidCrc32
        )
        .flatMap(header => read(input, header._1.toInt))
        .filterOrElse(
          data => {
            read(input, FooterLength)
              .forall { footer =>
                !checkCrc32 || hashBytes(data) == ByteBuffer
                  .wrap(footer)
                  .order(ByteOrder.LITTLE_ENDIAN)
                  .getInt
              }
          },
          InvalidCrc32
        )
    }

  def reader[F[_]: Sync](
      checkCrc32: Boolean
  ): Kleisli[F, InputStream, Either[ReadError, Array[Byte]]] =
    Kleisli(input => Sync[F].delay(reader_(checkCrc32).apply(input)))

  def typedReader[T: Parsable, F[_]: Sync](
      checkCrc32: Boolean
  ): Kleisli[F, InputStream, Either[ReadError, T]] = {
    TFRecord.reader[F](checkCrc32).andThen { elem =>
      elem match {
        case Left(value) =>
          Sync[F].delay(Left(value): Either[ReadError, T])
        case Right(value) =>
          implicitly[Parsable[T]].parser
            .andThen(ex => Sync[F].delay(Right(ex): Either[ReadError, T]))
            .run(value)
      }
    }
  }

  def streamReader[F[_]: Sync, A](
      reader: Kleisli[F, InputStream, Either[ReadError, A]]
  ): Kleisli[Stream[F, *], InputStream, Either[ReadError, A]] =
    Kleisli { input =>
      Stream
        .repeatEval(reader.apply(input))
        .repeatPull(_.uncons1.flatMap {
          case None                         => Pull.pure(None)
          case Some((Left(EmptyHeader), _)) => Pull.pure(None)
          case Some((elem, stream))         => Pull.output1(elem).as(Some(stream))
        })
    }

  def resourceReader[F[_]: Sync, A](
      reader: Kleisli[F, InputStream, Either[ReadError, A]]
  ): Kleisli[Stream[F, *], Resource[F, InputStream], Either[ReadError, A]] =
    Kleisli { resource =>
      Stream.resource(resource).flatMap(streamReader(reader).run)
    }

  private def read(
      input: InputStream,
      length: Int
  ): Either[ReadError, Array[Byte]] =
    Try {
      val data = Array.ofDim[Byte](length)
      var n = 0
      var off = 0
      while ({
        {
          n = input.read(data, off, data.length - off)
          if (n > 0) {
            off += n
          }
        }; n > 0 && off < data.length
      }) ()
      if (n <= 0) Array.emptyByteArray else data
    }.toEither.left.map(_ => ReadError)

}
