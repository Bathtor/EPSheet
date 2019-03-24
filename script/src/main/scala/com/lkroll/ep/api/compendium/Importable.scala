/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lars Kroll <bathtor@googlemail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.lkroll.ep.api.compendium

import scala.concurrent.Future
import com.lkroll.ep.compendium._
import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.ep.model.ActiveSkillSection

trait Importable {
  def updateLabel: String;
  def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String];
  def children: List[Importable] = Nil;
  def triggerWorkers(char: Character): Future[Unit] = Future.successful(());
}

object Importable {
  implicit def weapon2Import(w: Weapon): WeaponImport = WeaponImport(w);
  implicit def wwa2Import(w: WeaponWithAmmo): WeaponWithAmmoImport = WeaponWithAmmoImport(w);
  implicit def morphmodel2Import(m: MorphModel): MorphModelImport = MorphModelImport(m);
  implicit def morphinst2Import(m: MorphInstance): MorphInstanceImport = MorphInstanceImport(m);
  implicit def trait2Import(t: EPTrait): EgoTraitImport = EgoTraitImport(t);
  implicit def disorder2Import(d: Disorder): DisorderImport = DisorderImport(d);
  implicit def armour2Import(a: Armour): ArmourImport = ArmourImport(a);
  implicit def moddedarmour2Import(a: ModdedArmour): ModdedArmourImport = ModdedArmourImport(a);
  implicit def gear2Import(g: Gear): GearImport = GearImport(g);
  implicit def gearEntry2Import(e: GearEntry): GearEntryImport = GearEntryImport(e);
  implicit def software2Import(s: Software): SoftwareImport = SoftwareImport(s);
  implicit def substance2Import(s: Substance): SubstanceImport = SubstanceImport(s);
  implicit def skill2Import(s: CharacterSkill): SkillImport = SkillImport(s);
  implicit def skillDef2Import(s: SkillDef): SkillDefImport = SkillDefImport(s);
  implicit def psiSleight2Import(s: PsiSleight): SleightImport = SleightImport(s);

  def fromData(d: Data): Option[Importable] = d match {
    case a: Armour           => Some(a)
    case ma: ModdedArmour    => Some(ma)
    case wwa: WeaponWithAmmo => Some(wwa)
    case w: Weapon           => Some(w)
    case d: Derangement      => Some(DerangementImport(d, 0.0f))
    case d: Disorder         => Some(d)
    case t: EPTrait          => Some(t)
    case g: Gear             => Some(g)
    case s: Software         => Some(s)
    case s: Substance        => Some(s)
    case mm: MorphModel      => Some(mm)
    case mi: MorphInstance   => Some(mi)
    case ch: EPCharacter     => Some(new CharacterImport(ch))
    case s: CharacterSkill   => Some(s)
    case s: SkillDef         => Some(s)
    case s: PsiSleight       => Some(s)
    case _                   => None
  };
}

class ImportCache(val char: Character) {

  private var activeSkillNameToId: Option[Map[String, String]] = None;

  private def loadActiveSkillMap(): Map[String, String] = {
    val fields = char.repeating(ActiveSkillSection.skillName);
    val m = fields.map(attr => (attr() -> attr.getRowId.get)).toMap;
    activeSkillNameToId = Some(m);
    m
  }

  def activeSkillId(name: String): Option[String] = {
    val data = activeSkillNameToId match {
      case Some(m) => m
      case None    => loadActiveSkillMap()
    };
    data.get(name)
  }
  def reset(): Unit = {
    activeSkillNameToId = None;
  }
}
object ImportCache {
  def apply(char: Character): ImportCache = new ImportCache(char);
}
