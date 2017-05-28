package com.larskroll.ep.sheet

import org.scalatest._

class ParserTests extends FunSuite with Matchers {

  test("Should parse json formatted skills") {
    // #1
    val singleJsonData = """
{
  "skill": "Test",
  "field": "Test Field",
  "mod": -5
}
""";
    val resSingle = ValueParsers.skillsFrom(singleJsonData);
    resSingle should have length 1;
    val singleValue = resSingle(0);
    singleValue.skill shouldBe "Test";
    singleValue.field shouldBe Some("Test Field");
    singleValue.mod shouldBe -5;
    // #2
    val secondJsonData = """
{
  "skill": "Second Test",
  "mod": 10
}
""";
    val resSecond = ValueParsers.skillsFrom(secondJsonData);
    resSecond should have length 1;
    val secondValue = resSecond(0);
    secondValue.skill shouldBe "Second Test";
    secondValue.field shouldBe None;
    secondValue.mod shouldBe 10;
    // #3
    val multiJsonData = s"[$singleJsonData, $secondJsonData]";
    val resMulti = ValueParsers.skillsFrom(multiJsonData);
    resMulti should have length 2;
    val firstValue = resMulti(0);
    firstValue.skill shouldBe "Test";
    firstValue.field shouldBe Some("Test Field");
    firstValue.mod shouldBe -5;
    val secondValue2 = resMulti(1);
    secondValue2.skill shouldBe "Second Test";
    secondValue2.field shouldBe None;
    secondValue2.mod shouldBe 10;
  }

  test("Should parse text formatted skills") {
    // #1
    val singleData = "+30 Beam Weapons skill";
    val resSingle = ValueParsers.skillsFrom(singleData);
    resSingle should have length 1;
    val singleValue = resSingle(0);
    singleValue.skill shouldBe "Beam Weapons";
    singleValue.field shouldBe None;
    singleValue.mod shouldBe 30;
    // #2
    val singleDataField = "+30 Beam Weapons (Underwater)";
    val resSingleField = ValueParsers.skillsFrom(singleDataField);
    resSingleField should have length 1;
    val singleValueField = resSingleField(0);
    singleValueField.skill shouldBe "Beam Weapons";
    singleValueField.field shouldBe Some("Underwater");
    singleValueField.mod shouldBe 30;
    // #3
    val multiData = s"$singleData, $singleDataField";
    val resMulti = ValueParsers.skillsFrom(multiData);
    resMulti should have length 2;
    val firstValue = resMulti(0);
    firstValue.skill shouldBe "Beam Weapons";
    firstValue.field shouldBe None;
    firstValue.mod shouldBe 30;
    val secondValue2 = resMulti(1);
    secondValue2.skill shouldBe "Beam Weapons";
    secondValue2.field shouldBe Some("Underwater");
    secondValue2.mod shouldBe 30;
  }
}
