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

import cats.effect.Resource
import cats.effect.Sync
import com.google.cloud.storage.StorageOptions
import java.net.URI
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths

object Resources {

  final def stdin[F[_]: Sync] =
    Resource.fromAutoCloseable(Sync[F].delay(System.in))

  final def file[F[_]: Sync](path: String) =
    Resource.fromAutoCloseable(Sync[F].delay {
      URI.create(path) match {
        case gcsUri if gcsUri.getScheme == "gs" =>
          val service = StorageOptions.getDefaultInstance().getService()
          val blobPath = gcsUri.getPath().tail match {
            case s if s.endsWith("/") => s.init
            case s                    => s
          }
          val readChannel = service.reader(gcsUri.getHost(), blobPath)
          Channels.newInputStream(readChannel)
        case uri =>
          Files.newInputStream(Paths.get(uri.getPath()))
      }
    })

}
