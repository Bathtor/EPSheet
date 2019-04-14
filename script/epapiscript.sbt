enablePlugins(ScalaJSPlugin)
enablePlugins(BuildInfoPlugin)
import scala.sys.process._

name := "EP API Script"

organization := "com.lkroll.ep"

version := "1.0.1"

scalaVersion := "2.12.8"

resolvers += Resolver.bintrayRepo("lkrollcom", "maven")

libraryDependencies += "com.lkroll.roll20" %%% "roll20-api-framework" % "0.10.0"
libraryDependencies += "com.lkroll.ep" %%% "epcompendium-core" % "5.0.0"
libraryDependencies += "com.lkroll.ep" %%% "ep-model" % "1.12.2"
libraryDependencies += "com.lkroll.common" %%% "common-data-tools" % "1.3.+"
libraryDependencies += "com.lihaoyi" %%% "fastparse" % "1.+"
libraryDependencies += "org.rogach" %%% "scallop" % "3.1.+"
libraryDependencies += "org.scalactic" %%% "scalactic" % "3.0.4" % "test"
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % "test"

lazy val submitScript = taskKey[Unit]("Submit the script that assembles the API script");
lazy val openScript = taskKey[Unit]("Opens the API script");
lazy val submit = taskKey[Unit]("Assemble and fastOpt the API script");
lazy val submitScriptFull = taskKey[Unit]("Submit the script that assembles the API script in fullOpt");
lazy val submitFull = taskKey[Unit]("Assemble and fullOpt the API script");

submitScript := {
  s"./assemble.sc --version ${version.value}" !
}

submitScriptFull := {
  s"./assemble.sc --version ${version.value} --full true" !
}

openScript := {
  Seq("/Applications/Sublime Text.app/Contents/SharedSupport/bin/subl", "ep-script.js").!!
}

submit in Compile := Def.sequential(
  fastOptJS in Compile,
  submitScript in Compile,
  openScript in Compile
).value

submitFull in Compile := Def.sequential(
  fullOptJS in Compile,
  submitScriptFull in Compile,
  openScript in Compile
).value

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "com.lkroll.ep.api"
