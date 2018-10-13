name := "tactical"
description := "Referenced based java bytecode intermediation"

ThisBuild / organization := "me.aki.tactical"
ThisBuild / version := "0.1"

val javaSettings = Seq(
  crossPaths := false, // do not append scala version to artifact names
  autoScalaLibrary := false // do not use scala runtime dependency
)

val asmVersion = "6.2.1"

// INTERMEDIATIONS
lazy val core = (project in file ("core"))
  .settings(javaSettings)

lazy val stack = (project in file ("stack"))
  .dependsOn(core)
  .settings(javaSettings)

lazy val ref = (project in file ("ref"))
  .dependsOn(core)
  .settings(javaSettings)

lazy val dex = (project in file("dex"))
  .dependsOn(core)
  .settings(javaSettings)

// TEXTIFIER & PARSER
lazy val coreTextifier = (project in file ("textify-core"))
  .dependsOn(core)
  .settings(javaSettings)

lazy val coreParser = (project in file ("parser-core"))
  .dependsOn(core)
  .settings(
    libraryDependencies += "com.lihaoyi" %% "fastparse" % "1.0.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
  )

// CONVERSION
lazy val stackConversionUtils = (project in file ("stack conversion-utils"))
  .dependsOn(stack)
  .settings(javaSettings)

lazy val asmStackConversion = (project in file ("conversion-asm-stack"))
  .dependsOn(stack, stackConversionUtils)
  .settings(javaSettings)
  .settings(
    libraryDependencies += "org.ow2.asm" % "asm" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-tree" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-commons" % asmVersion
  )

lazy val refStackConversion = (project in file ("conversion-stack-ref"))
  .dependsOn(stack, ref, stackConversionUtils)
  .settings(javaSettings)
