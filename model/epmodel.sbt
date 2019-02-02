enablePlugins(ScalaJSPlugin)
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "EP Model Root"

organization in ThisBuild := "com.lkroll.ep"

version in ThisBuild := "1.11.0"

scalaVersion in ThisBuild := "2.12.6"

resolvers += "Apache" at "http://repo.maven.apache.org/maven2"
resolvers += Resolver.bintrayRepo("lkrollcom", "maven")
resolvers += Resolver.mavenLocal

lazy val root = project.in(file(".")).
  aggregate(epmodelJS, epmodelJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val epmodel = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    name := "EP Model",
    libraryDependencies += "com.lkroll.roll20" %%% "roll20-sheet-model" % "0.10.+", 
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % "test",
    EclipseKeys.useProjectId := true,
    EclipseKeys.eclipseOutput := Some("./etarget"),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.lkroll.ep.model"
    //EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed
  ).
  jvmSettings(
    // Add JVM-specific settings here
    //name := "EP Model JVM",
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
  ).
  jsSettings(
    // Add JS-specific settings here
    //name := "EP Model JS",
    libraryDependencies += "com.lkroll.roll20" %%% "roll20-sheet-facade" % "1.+" % "provided"
  )

lazy val epmodelJVM = epmodel.jvm
lazy val epmodelJS = epmodel.js
