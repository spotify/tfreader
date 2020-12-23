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
import io.circe.{Printer => CircePrinter, Encoder, Json}
import java.lang.{Float => JFloat, Long => JLong}

import com.google.protobuf.ByteString
import org.tensorflow.example.{
  BytesList,
  Example,
  Feature,
  Features,
  FloatList,
  Int64List
}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

trait ExampleInstances extends ExampleShowInstances with ExampleEncoderInstances

trait ExampleShowInstances {
  given showExample(using encoder: Encoder[Example]): Show[Example] with {
      private[this] val Printer =
        CircePrinter.noSpaces.copy(dropNullValues = true)

      override def show(t: Example): String =
        Printer.print(Encoder[Example].apply(t))
    }
}

trait ExampleEncoderInstances extends ProtobufEncoderInstances {

  given bytesListEncoder: Encoder[BytesList] with {
      override def apply(a: BytesList): Json =
        Json.obj(
          "value" -> Encoder
            .encodeIterable[ByteString, Iterable]
            .contramap[BytesList](_.getValueList.asScala)
            .apply(a)
        )
    }

  given floatListEncoder: Encoder[FloatList] with {
      override def apply(a: FloatList): Json =
        Json.obj(
          "value" -> Encoder
            .encodeIterable[JFloat, Iterable]
            .contramap[FloatList](_.getValueList.asScala)
            .apply(a)
        )
    }

  given int64ListEncoder: Encoder[Int64List] with {
      override def apply(a: Int64List): Json =
        Json.obj(
          "value" -> Encoder
            .encodeIterable[JLong, Iterable]
            .contramap[Int64List](_.getValueList.asScala)
            .apply(a)
        )
    }

  given featureEncoder: Encoder[Feature] with {
    final def apply(f: Feature): Json =
      f match {
        case _ if f.hasBytesList =>
          Json.obj("bytesList" -> Encoder[BytesList].apply(f.getBytesList))
        case _ if f.hasFloatList =>
          Json.obj("floatList" -> Encoder[FloatList].apply(f.getFloatList))
        case _ if f.hasInt64List =>
          Json.obj("int64List" -> Encoder[Int64List].apply(f.getInt64List))
        case _ => Json.Null
      }
  }

  given featuresEncoder: Encoder[Features] with {
    override def apply(a: Features): Json =
      Json.obj(
        "feature" -> Encoder
          .encodeMapLike[String, Feature, mutable.Map]
          .contramap[Features](_.getFeatureMap.asScala)
          .apply(a)
      )
  }

  given exampleEncoder: Encoder[Example] with {
    override def apply(a: Example): Json =
      Json.obj(
        "features" -> Encoder[Features]
          .contramap[Example](_.getFeatures)
          .apply(a)
      )
  }

  object flat {
    given featureEncoder: Encoder[Feature] with {
      final def apply(f: Feature): Json =
        f match {
          case _ if f.hasBytesList => Encoder[BytesList].apply(f.getBytesList)
          case _ if f.hasFloatList => Encoder[FloatList].apply(f.getFloatList)
          case _ if f.hasInt64List => Encoder[Int64List].apply(f.getInt64List)
          case _                   => Json.Null
        }
    }

    given featuresEncoder: Encoder[Features] = Encoder
      .encodeMapLike[String, Feature, mutable.Map]
      .contramap(_.getFeatureMap.asScala)

    given exampleEncoder: Encoder[Example] =
      Encoder[Features].contramap(_.getFeatures)

    given bytesListEncoder: Encoder[BytesList] = Encoder
      .encodeIterable[ByteString, Iterable]
      .contramap(_.getValueList.asScala)

    given floatListEncoder: Encoder[FloatList] =
      Encoder.encodeIterable[JFloat, Iterable].contramap(_.getValueList.asScala)

    given int64ListEncoder: Encoder[Int64List] =
      Encoder.encodeIterable[JLong, Iterable].contramap(_.getValueList.asScala)
  }
}
