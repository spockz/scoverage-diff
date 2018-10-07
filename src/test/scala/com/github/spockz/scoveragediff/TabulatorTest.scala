package com.github.spockz.scoveragediff

import org.scalatest.{FeatureSpec, Matchers}

class TabulatorTest extends FeatureSpec with Matchers {

  feature("Tabulator") {
    scenario("should print simple table correctly") {
      val expected =
        """|| class name           | previous coverage | current coverage |
          || -------------------- | ----------------- | ---------------- |
          || FooBarEncryptionUtil | 90.00             | 67.14            |
          || EncryptionUtil       | 97.48             | 52.94            |""".stripMargin

      val input =
        Seq(
          Seq("class name", "previous coverage", "current coverage"),
          Seq("FooBarEncryptionUtil", "90.00", "67.14"),
          Seq("EncryptionUtil", "97.48", "52.94")
        )

      Tabulator(input).toString() shouldBe expected
    }
  }
}
