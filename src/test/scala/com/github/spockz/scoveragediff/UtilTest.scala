package com.github.spockz.scoveragediff

import org.scalatest.{FeatureSpec, Matchers}

class UtilTest extends FeatureSpec with Matchers {
  feature("Util.merge") {
    scenario("merge two sequences and retain sequences that exist on either side") {
      val left: Map[Char, Seq[Int]] =
        Map (
          'a' -> (1 to 5),
          'b' -> (1 to 5),
        )

      val right: Map[Char, Seq[Int]] =
        Map (
          'b' -> (6 to 10),
          'c' -> (1 to 5)
        )

      val expectedResult =
        Map (
          'a' -> left('a'),
          'b' -> (left('b') ++ right('b')),
          'c' -> right('c')
        )


      Util.merge(left, right) shouldBe expectedResult
    }
  }
}
