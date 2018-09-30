name := "tactical"
description := "Referenced based java bytecode intermediation"

ThisBuild / organization := "me.aki.tactical"
ThisBuild / version := "0.1"

ThisBuild / crossPaths := false // do not append scala version to artifact names
ThisBuild / autoScalaLibrary := false // do not use scala runtime dependency

val asmVersion = "6.2.1"

lazy val core = project in file ("core")

lazy val stack = (project in file ("stack"))
  .dependsOn(core)

lazy val ref = (project in file ("ref"))
  .dependsOn(core)

lazy val asmStackConversion = (project in file ("conversion-asm-stack"))
  .dependsOn(stack)
  .settings(
    libraryDependencies += "org.ow2.asm" % "asm" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-tree" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-commons" % asmVersion
  )

lazy val refStackConversion = (project in file ("conversion-stack-ref"))
  .dependsOn(stack, ref)
