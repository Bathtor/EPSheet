enablePlugins(ScalaJSPlugin)
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import scala.sys.process._

name := "EP Sheet Root"

organization in ThisBuild := "com.lkroll.ep"

version in ThisBuild := "1.13.1"

scalaVersion in ThisBuild := "2.12.10"

resolvers in ThisBuild += "Apache" at "https://repo.maven.apache.org/maven2"
resolvers in ThisBuild += Resolver.bintrayRepo("lkrollcom", "maven")
resolvers in ThisBuild += Resolver.mavenLocal

lazy val submitSheet = taskKey[Unit]("Submit the script that assembled and uploads the sheet");
lazy val submit = taskKey[Unit]("Assemble and fastOpt, and then upload the sheet");
lazy val submitSheetFull = taskKey[Unit]("Submit the script that assembled and uploads the sheet in fullOpt");
lazy val submitFull = taskKey[Unit]("Assemble and fullOpt, and then upload the sheet");

submitSheet := {
  s"./assemble.sc --version ${version.value}" !
}

submitSheetFull := {
  s"./assemble.sc --version ${version.value} --full true" !
}

lazy val root = project
  .in(file("."))
  .aggregate(epsheetJS, epsheetJVM)
  .settings(
    publish := {},
    publishLocal := {},
    submit in Compile := Def
      .sequential(
        assembly in Compile in epsheetJVM,
        fastOptJS in Compile in epsheetJS,
        submitSheet in Compile
      )
      .value,
    submitFull in Compile := Def
      .sequential(
        assembly in Compile in epsheetJVM,
        fullOptJS in Compile in epsheetJS,
        submitSheetFull in Compile
      )
      .value
  )

lazy val epsheet = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "EP Sheet",
    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.6.+",
    libraryDependencies += "com.lkroll.roll20" %%% "roll20-sheet-framework" % "0.11.1",
    libraryDependencies += "com.lkroll.ep" %%% "ep-model" % version.value,
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.8" % "test",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.lkroll.ep.sheet"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    //name := "EP Sheet JVM",
    mainClass in assembly := Some("com.lkroll.roll20.sheet.Packager"),
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
  )
  .jsSettings(
    // Add JS-specific settings here
    //name := "EP Sheet JS",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.+",
    libraryDependencies += "com.lkroll.roll20" %%% "roll20-sheet-facade" % "1.+" % "provided"
  )

lazy val epsheetJVM = epsheet.jvm
lazy val epsheetJS = epsheet.js
