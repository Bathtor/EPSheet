enablePlugins(ScalaJSPlugin)
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "EP Model Root"

ThisBuild / organization := "com.lkroll.ep"

ThisBuild / version := "1.13.2"

ThisBuild / scalaVersion := "2.13.7"

ThisBuild / resolvers += "Apache" at "https://repo.maven.apache.org/maven2"
ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = project
  .in(file("."))
  .aggregate(epmodelJS, epmodelJVM)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val epmodel = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "EP Model",
    libraryDependencies += "com.lkroll" %%% "roll20-sheet-model" % "0.12.0-SNAPSHOT",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.10" % "test",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.lkroll.ep.model"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    //name := "EP Model JVM",
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
  )
  .jsSettings(
    // Add JS-specific settings here
    //name := "EP Model JS",
    libraryDependencies += "com.lkroll" %%% "roll20-sheet-facade" % "1.+" % "provided"
  )

lazy val epmodelJVM = epmodel.jvm
lazy val epmodelJS = epmodel.js
