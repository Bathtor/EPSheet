enablePlugins(ScalaJSPlugin)
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import scala.sys.process._

name := "EP Sheet Root"

ThisBuild / organization := "com.lkroll.ep"

ThisBuild / version := "1.13.2"

ThisBuild / scalaVersion := "2.13.7"

ThisBuild / resolvers += "Apache" at "https://repo.maven.apache.org/maven2"
ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.mavenLocal

ThisBuild / scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions"
)

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
    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.11.0",
    libraryDependencies += "com.lkroll" %%% "roll20-sheet-framework" % "0.12.0-SNAPSHOT",
    libraryDependencies += "com.lkroll.ep" %%% "ep-model" % version.value,
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.10" % "test",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.lkroll.ep.sheet"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    //name := "EP Sheet JVM",
    mainClass in assembly := Some("com.lkroll.roll20.sheet.Packager"),
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
  )
  .jsSettings(
    // Add JS-specific settings here
    //name := "EP Sheet JS",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    libraryDependencies += "com.lkroll" %%% "roll20-sheet-facade" % "1.+" % "provided"
  )

lazy val epsheetJVM = epsheet.jvm
lazy val epsheetJS = epsheet.js
