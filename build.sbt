name := "tactical"
description := "Referenced based java bytecode intermediation"

ThisBuild / organization := "me.aki.tactical"
ThisBuild / version := "0.1"

val javaSettings = Seq(
  crossPaths := false, // do not append scala version to artifact names
  autoScalaLibrary := false, // do not use scala runtime dependency
  javacOptions ++= Seq("-source", "1.10", "-target", "1.10", "-Xlint")
)

val parserSettings = Seq(
  libraryDependencies += "com.lihaoyi" %% "fastparse" % "1.0.0",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
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

// TEXTIFIER & PARSER
lazy val coreTextifier = (project in file ("textify-core"))
  .dependsOn(core)
  .settings(javaSettings)

lazy val stackTextifier = (project in file ("textify-stack"))
  .dependsOn(stack, coreTextifier)
  .settings(javaSettings)

lazy val refTextifier = (project in file ("textify-ref"))
  .dependsOn(ref, coreTextifier)
  .settings(javaSettings)

lazy val coreParser = (project in file ("parser-core"))
  .dependsOn(core, coreTextifier % Test)
  .settings(parserSettings)

lazy val stackParser = (project in file ("parser-stack"))
  .dependsOn(stack, coreParser, stackTextifier % Test)
  .settings(parserSettings)

lazy val refParser = (project in file ("parser-ref"))
  .dependsOn(ref, coreParser, refTextifier % Test)
  .settings(parserSettings)

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
