package com.lkroll.ep.model

import org.scalatest._
import org.scalactic.source.Position.apply

class ParserTests extends FunSuite with Matchers {

  test("Should parse simple aptitudes") {
    val singleNumber = "30";
    val resSingleNumberT = ValueParsers.aptitudesFrom(singleNumber);
    val resSingleNumber = resSingleNumberT.get;
    resSingleNumber.cog shouldBe Some(30);
    resSingleNumber.coo shouldBe Some(30);
    resSingleNumber.int shouldBe Some(30);
    resSingleNumber.ref shouldBe Some(30);
    resSingleNumber.sav shouldBe Some(30);
    resSingleNumber.som shouldBe Some(30);
    resSingleNumber.wil shouldBe Some(30);
  }

  test("Should parse named aptitudes") {
    val commaApts = "+10 COG, -5 SAV";
    val resCommaT = ValueParsers.aptitudesFrom(commaApts);
    val resComma = resCommaT.get;
    resComma.cog shouldBe Some(10);
    resComma.coo shouldBe None;
    resComma.int shouldBe None;
    resComma.ref shouldBe None;
    resComma.sav shouldBe Some(-5);
    resComma.som shouldBe None;
    resComma.wil shouldBe None;
  }

  test("Should parse json aptitudes") {
    val jsonApts = """
{
  "cog": 30,
  "coo": 25,
  "int": 25,
  "ref": 25,
  "sav": 15,
  "som": 25
}
""";
    val resJsonT = ValueParsers.aptitudesFrom(jsonApts);
    val resJson = resJsonT.get;
    resJson.cog shouldBe Some(30);
    resJson.coo shouldBe Some(25);
    resJson.int shouldBe Some(25);
    resJson.ref shouldBe Some(25);
    resJson.sav shouldBe Some(15);
    resJson.som shouldBe Some(25);
    resJson.wil shouldBe None;
  }

  test("Should parse json formatted skills") {
    // #1
    val singleJsonData = """
{
  "skill": "Test",
  "field": "Test Field",
  "mod": -5
}
""";
    val resSingle = ValueParsers.skillsFrom(singleJsonData).get;
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
    val resSecond = ValueParsers.skillsFrom(secondJsonData).get;
    resSecond should have length 1;
    val secondValue = resSecond(0);
    secondValue.skill shouldBe "Second Test";
    secondValue.field shouldBe None;
    secondValue.mod shouldBe 10;
    // #3
    val multiJsonData = s"[$singleJsonData, $secondJsonData]";
    val resMulti = ValueParsers.skillsFrom(multiJsonData).get;
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
    val resSingle = ValueParsers.skillsFrom(singleData).get;
    resSingle should have length 1;
    val singleValue = resSingle(0);
    singleValue.skill shouldBe "Beam Weapons";
    singleValue.field shouldBe None;
    singleValue.mod shouldBe 30;
    // #2
    val singleDataField = "+30 Beam Weapons (Underwater)";
    val resSingleField = ValueParsers.skillsFrom(singleDataField).get;
    resSingleField should have length 1;
    val singleValueField = resSingleField(0);
    singleValueField.skill shouldBe "Beam Weapons";
    singleValueField.field shouldBe Some("Underwater");
    singleValueField.mod shouldBe 30;
    // #3
    val multiData = s"$singleData, $singleDataField";
    val resMulti = ValueParsers.skillsFrom(multiData).get;
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
