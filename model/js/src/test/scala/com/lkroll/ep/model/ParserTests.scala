package com.lkroll.ep.model

import org.scalatest._
import funsuite._
import matchers._
import org.scalactic.source.Position.apply

class ParserTests extends AnyFunSuite with should.Matchers {

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

  private def parseSingleEffect(s: String): Effect = {
    val res = ValueParsers.effectsFrom(s).get;
    res should have length 1;
    res(0)
  }

  test("Should parse speed mods") {
    val positiveMod = "+2 SPD";
    val SpeedMod(mod) = parseSingleEffect(positiveMod);
    mod shouldBe 2;
    val negativeMod = "-1 SPD";
    val SpeedMod(mod2) = parseSingleEffect(negativeMod);
    mod2 shouldBe -1;
  }

  test("Should parse moa mods") {
    val positiveMod = "+2 MOA";
    val MOAMod(mod) = parseSingleEffect(positiveMod);
    mod shouldBe 2;
    val negativeMod = "-1 MOA";
    val MOAMod(mod2) = parseSingleEffect(negativeMod);
    mod2 shouldBe -1;
  }

  test("Should parse ini mods") {
    val positiveMod = "+2 INI";
    val IniMod(mod) = parseSingleEffect(positiveMod);
    mod shouldBe 2;
    val negativeMod = "-1 INI";
    val IniMod(mod2) = parseSingleEffect(negativeMod);
    mod2 shouldBe -1;
  }

  test("Should parse aptitude mods") {
    for (apt <- Aptitude.values) {
      val positiveMod = "+2 " + apt.toString;
      val AptitudeMod(apt1, mod) = parseSingleEffect(positiveMod);
      apt1 shouldBe apt;
      mod shouldBe 2;
      val negativeMod = "-1 " + apt.toString;
      val AptitudeMod(apt2, mod2) = parseSingleEffect(negativeMod);
      apt2 shouldBe apt;
      mod2 shouldBe -1;
    }
  }

  test("Should parse skill mods") {
    // #1
    val singleData = "+30 Beam Weapons skill";
    val SkillMod(skill, field, mod) = parseSingleEffect(singleData);
    skill shouldBe "Beam Weapons";
    field shouldBe None;
    mod shouldBe 30;
    // #2
    val singleDataField = "+30 Beam Weapons (Underwater)";
    val SkillMod(skill2, field2, mod2) = parseSingleEffect(singleDataField);
    skill2 shouldBe "Beam Weapons";
    field2 shouldBe Some("Underwater");
    mod2 shouldBe 30;
  }

  test("Should parse ignore traumas") {
    val effect = "Ignore modifiers from 2 traumas";
    val IgnoreTraumas(n) = parseSingleEffect(effect);
    n shouldBe 2;
  }

  test("Should parse ignore wounds") {
    val effect = "Ignore modifiers from 2 wounds";
    val IgnoreWounds(n) = parseSingleEffect(effect);
    n shouldBe 2;
  }

  test("Should parse DUR mods") {
    val positiveMod = "+2 DUR";
    val DurMod(mod) = parseSingleEffect(positiveMod);
    mod shouldBe 2;
    val negativeMod = "-1 DUR";
    val DurMod(mod2) = parseSingleEffect(negativeMod);
    mod2 shouldBe -1;
  }

  test("Should parse LUC mods") {
    val positiveMod = "+2 LUC";
    val LucMod(mod) = parseSingleEffect(positiveMod);
    mod shouldBe 2;
    val negativeMod = "-1 LUC";
    val LucMod(mod2) = parseSingleEffect(negativeMod);
    mod2 shouldBe -1;
  }

  test("Should parse freeform effects") {
    val effect = "This can literally be any arbitrary text";
    val FreeForm(s) = parseSingleEffect(effect);
    s shouldBe effect;
  }

  test("Should parse multiple effects") {
    val effects =
      "+30 Beam Weapons (Underwater), Ignore modifiers from 2 traumas, +2 LUC, This can literally be any arbitrary text";
    val res = ValueParsers.effectsFrom(effects).get;
    res should have length 4;
    val SkillMod("Beam Weapons", Some("Underwater"), 30) = res(0);
    val IgnoreTraumas(2) = res(1);
    val LucMod(2) = res(2);
    val FreeForm("This can literally be any arbitrary text") = res(3);
  }

}
