name := "tactical"
description := "Referenced based java bytecode intermediation"

ThisBuild / organization := "me.aki.tactical"
ThisBuild / version := "0.1"

// necessary while we use a custom dexlib2 build
ThisBuild / resolvers += Resolver.mavenLocal
//

val javaVersion = "1.10"

val asmVersion = "7.0"
val smaliVersion = "2.2.7-cb231dd4" // https://github.com/JesusFreke/smali/pull/705/commits/cb231dd48c34a0eda5fb346fc55674efbcc9a855

val fastparseVersion = "1.0.0"

val junitVersion = "5.5.1"
val scalaTestVersion = "3.0.5"
val scalaCheckVersion = "1.14.0"

def javaSettings(config: Configuration) = Seq(
  config / crossPaths := false, // do not append scala version to artifact names
  config / autoScalaLibrary := false, // do not use scala runtime dependency
  config / javacOptions ++= Seq("-source", javaVersion),
  config / compile / javacOptions ++= Seq("-target", javaVersion, "-Xlint")
)

lazy val parserSettings = Seq(
  libraryDependencies += "com.lihaoyi" %% "fastparse" % fastparseVersion
)

lazy val scalaTest = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  libraryDependencies += "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test
)

lazy val javaTest = Seq(
  libraryDependencies += "org.junit.jupiter" % "junit-jupiter-api" % junitVersion % Test
)

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

lazy val textifierDex = (project in file("textify/dex"))
  .dependsOn(dex, textifierCore)
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
lazy val conversionAsmStack = (project in file ("conversion/asm-stack"))
  .dependsOn(stack, utilsStack)
  .settings(javaSettings(Compile))
  .settings(
    libraryDependencies += "org.ow2.asm" % "asm" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-tree" % asmVersion,
    libraryDependencies += "org.ow2.asm" % "asm-commons" % asmVersion
  )

lazy val conversionRefStack = (project in file ("conversion/stack-ref"))
  .dependsOn(stack, ref, utilsStack, utilsRef)
  .settings(javaSettings(Compile))
  .settings(scalaTest)

lazy val conversionSmaliDex = (project in file ("conversion/smali-dex"))
  .dependsOn(dex, utilsDex)
  .settings(javaSettings(Compile))
  .settings(javaTest)
  .settings(
    libraryDependencies += "org.smali" % "dexlib2" % smaliVersion
  )

// UTILITIES
lazy val utilsCore = (project in file ("utils/core"))
  .dependsOn(core)
  .settings(javaSettings(Compile))

lazy val utilsStack = (project in file ("utils/stack"))
  .dependsOn(utilsCore, stack)
  .settings(javaSettings(Compile))

lazy val utilsRef = (project in file ("utils/ref"))
  .dependsOn(utilsCore, ref)
  .settings(javaSettings(Compile))

lazy val utilsDex = (project in file ("utils/dex"))
  .dependsOn(utilsCore, dex)
  .settings(javaSettings(Compile))
  .settings(javaTest)
  .settings(
    libraryDependencies += "org.smali" % "dexlib2" % smaliVersion,
    libraryDependencies += "org.smali" % "smali" % smaliVersion
  )
