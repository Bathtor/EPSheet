enablePlugins(ScalaJSPlugin)
enablePlugins(BuildInfoPlugin)

name := "EP API Script"

organization := "com.lkroll.ep"

version := "0.4.0"

scalaVersion := "2.12.4"

libraryDependencies += "com.lkroll.roll20" %%% "roll20-api-framework" % "0.4.+"
libraryDependencies += "com.lkroll.ep" %%% "ep-model" % "1.4.3"
libraryDependencies += "com.lihaoyi" %%% "fastparse" % "1.+"
libraryDependencies += "org.rogach" %%% "scallop" % "3.1.+"
libraryDependencies += "org.scalactic" %%% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % "test"

lazy val submitScript = taskKey[Unit]("Submit the script that assembles the API script");
lazy val submit = taskKey[Unit]("Assemble and fastOpt the API script");
lazy val submitScriptFull = taskKey[Unit]("Submit the script that assembles the API script in fullOpt");
lazy val submitFull = taskKey[Unit]("Assemble and fullOpt the API script");

submitScript := {
  s"./assemble.sc --version ${version.value}" !
}

submitScriptFull := {
  s"./assemble.sc --version ${version.value} --full true" !
}

submit in Compile := Def.sequential(
  fastOptJS in Compile,
  submitScript in Compile
).value

submitFull in Compile := Def.sequential(
  fullOptJS in Compile,
  submitScriptFull in Compile
).value

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "com.lkroll.ep.api"