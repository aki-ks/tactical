name := "tactical"
description := "Referenced based java bytecode intermediation"

ThisBuild / organization := "me.aki.tactical"
ThisBuild / version := "0.1"

def javaSettings(config: Configuration) = Seq(
  config / crossPaths := false, // do not append scala version to artifact names
  config / autoScalaLibrary := false, // do not use scala runtime dependency
  config / javacOptions ++= Seq("-source", "1.10", "-target", "1.10", "-Xlint")
)

lazy val parserSettings = Seq(
  libraryDependencies += "com.lihaoyi" %% "fastparse" % "1.0.0"
)

lazy val scalaTest = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)

val asmVersion = "6.2.1"

// INTERMEDIATIONS
lazy val core = (project in file ("core"))
  .settings(javaSettings(Compile))

lazy val stack = (project in file ("stack"))
  .dependsOn(core)
  .settings(javaSettings(Compile))

lazy val ref = (project in file ("ref"))
  .dependsOn(core)
  .settings(javaSettings(Compile))

lazy val dex = (project in file("dex"))
  .dependsOn(core)
  .settings(javaSettings(Compile))

// TEXTIFIER & PARSER
lazy val textifierCore = (project in file("textify/core"))
  .dependsOn(core)
  .settings(javaSettings(Compile))

lazy val textifierStack = (project in file ("textify/stack"))
  .dependsOn(stack, textifierCore)
  .settings(javaSettings(Compile))

lazy val textifierRef = (project in file ("textify/ref"))
  .dependsOn(ref, textifierCore)
  .settings(javaSettings(Compile))

lazy val parserCore = (project in file ("parser/core"))
  .dependsOn(core, textifierCore % Test)
  .settings(parserSettings)
  .settings(scalaTest)

lazy val parserStack = (project in file ("parser/stack"))
  .dependsOn(stack, parserCore, textifierStack % Test)
  .settings(parserSettings)
  .settings(scalaTest)

lazy val parserRef = (project in file ("parser/ref"))
  .dependsOn(ref, parserCore % "test->test;compile->compile", textifierRef % Test)
  .settings(parserSettings)
  .settings(scalaTest)

// CONVERSION
lazy val conversionStackUtils = (project in file ("conversion/stack-utils"))
  .dependsOn(stack)
  .settings(javaSettings(Compile))

lazy val conversionAsmStack = (project in file ("conversion/asm-stack"))
  .dependsOn(stack, conversionStackUtils)
  .settings(javaSettings(Compile))
  .settings(
    libraryDependencies += "org.ow2.asm" % "asm" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-tree" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-commons" % asmVersion
  )

lazy val conversionRefStack = (project in file ("conversion/stack-ref"))
  .dependsOn(stack, ref, conversionStackUtils)
  .settings(javaSettings(Compile))
  .settings(scalaTest)
