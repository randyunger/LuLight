package org.runger.lulight

/**
  *
  * Created by Unger on 3/4/16.
  *
  **/


import org.specs2.mutable.Specification

class UtilsTest extends Specification {

  "UtilsTest" should {
    "fileFromUri" in {
      val uri = "/up"
      val f = Utils.fileFromUri(uri)
      f shouldEqual "up"
      ok
    }

    "with two slashes" in {
      val uri = "/some/up"
      val f = Utils.fileFromUri(uri)
      f shouldEqual "up"
      ok
    }

    "with two slashes" in {
      val uri = "eh/some/up"
      val f = Utils.fileFromUri(uri)
      f shouldEqual "up"
      ok
    }

    "with no slash" in {
      val uri = "up"
      val f = Utils.fileFromUri(uri)
      f shouldEqual "up"
      ok
    }

  }
}
