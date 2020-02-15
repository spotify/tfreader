package tfr

import java.io.{File, FileInputStream}

import cats.effect.IO

class TfrSuite extends munit.FunSuite {
  private[this] val ResourceFile = new File(
    getClass.getResource("/part-00000-of-00004.tfrecords").toURI
  )

  test("read as example") {
    val example = TFRecord
      .readerAsExample[IO](false)
      .apply(new FileInputStream(ResourceFile))
      .attempt
      .unsafeRunSync()

    assert(example.isRight)
  }

  test("read as example with crc32 check") {
    val example = TFRecord
      .readerAsExample[IO](true)
      .apply(new FileInputStream(ResourceFile))
      .attempt
      .unsafeRunSync()

    assert(example.isRight)
  }

  test("read all as example") {
    val examples = TFRecord
      .streamReader(TFRecord.readerAsExample[IO](false))
      .apply(new FileInputStream(ResourceFile))
      .compile
      .toList
      .unsafeRunSync()
      .filter(_.isRight)

    assertEquals(examples.length, 3750)
  }

}
