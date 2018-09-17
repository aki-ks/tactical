name := "tactical"
description := "Referenced based java bytecode intermediation"

ThisBuild / organization := "me.aki.tactical"
ThisBuild / version := "0.1"

ThisBuild / crossPaths := false // do not append scala version to artifact names
ThisBuild / autoScalaLibrary := false // do not use scala runtime dependency

lazy val core = project in file ("core")

lazy val stack = (project in file ("stack"))
  .dependsOn(core)

lazy val asmStackConversion = (project in file("conversion-asm-stack"))
  .dependsOn(stack)
  .settings(
    libraryDependencies += "org.ow2.asm" % "asm" % "6.2.1"
  )
