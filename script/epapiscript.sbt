enablePlugins(ScalaJSPlugin)
enablePlugins(JSDependenciesPlugin)
enablePlugins(BuildInfoPlugin)
import scala.sys.process._

name := "EP API Script"

organization := "com.lkroll.ep"

version := "1.0.4"

scalaVersion := "2.13.7"

resolvers += "Apache" at "https://repo.maven.apache.org/maven2"
resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.mavenLocal

libraryDependencies += "com.lkroll" %%% "roll20-api-framework" % "0.11.3"
libraryDependencies += "com.lkroll" %%% "epcompendium-core" % "6.1.1"
libraryDependencies += "com.lkroll.ep" %%% "ep-model" % "1.14.0"
libraryDependencies += "com.lkroll" %%% "common-data-tools" % "1.3.3"
libraryDependencies += "com.lihaoyi" %%% "fastparse" % "2.3.3"
libraryDependencies += "org.rogach" %%% "scallop" % "4.1.0"
libraryDependencies += "com.outr" %%% "scribe" % "3.6.3"
libraryDependencies += "org.scalactic" %%% "scalactic" % "3.2.10" % "test"
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.10" % "test"

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
skip in packageJSDependencies := false
