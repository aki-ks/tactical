package me.aki.tactical.core.parser.test

import scala.collection.JavaConverters._
import java.util.{Optional, List => JList, Set => JSet}

import me.aki.tactical.core.{Module, Path}
import me.aki.tactical.core.parser.ModuleParser
import me.aki.tactical.core.textify.ModuleTextifier
import org.scalatest.{FlatSpec, Matchers}

class ModuleTest extends FlatSpec with Matchers {
  "The ModuleParser" should "parse a basic module skeleton" in {
    ModuleParser.parse("module java.lang.base {}") shouldEqual
      new Module(Path.of("java", "lang", "base"))
  }

  it should "parse module versions" in {
    ModuleParser.parse("module foo { version \"1.0\"; }").getVersion shouldEqual Optional.of("1.0")
  }

  it should "parse main classes" in {
    ModuleParser.parse("module foo { main com.example.Main; }").getMainClass shouldEqual Optional.of(Path.of("com", "example", "Main"))
  }

  it should "parse packages" in {
    ModuleParser.parse("module foo { package com.example.foo; }").getPackages shouldEqual List(Path.of("com", "example", "foo")).asJava
  }

  it should "parse requires" in {
    ModuleParser.parse("module foo { requires com.example.foo; }").getRequires shouldEqual
      List(new Module.Require(Path.of("com", "example", "foo"), JSet.of(), Optional.empty())).asJava

    ModuleParser.parse("module foo { requires com.example.foo : \"4.8\"; }").getRequires shouldEqual
      List(new Module.Require(Path.of("com", "example", "foo"), JSet.of(), Optional.of("4.8"))).asJava
  }

  it should "parse exports" in {
    ModuleParser.parse("module foo { exports com.example.foo to com.example.bar, com.example.baz; }").getExports shouldEqual
      List(new Module.Export(Path.of("com", "example", "foo"), JSet.of(), JList.of(Path.of("com", "example", "bar"), Path.of("com", "example", "baz")))).asJava
  }

  it should "parse opens" in {
    ModuleParser.parse("module foo { opens com.example.foo to com.example.bar, com.example.baz; }").getOpens shouldEqual
      List(new Module.Open(Path.of("com", "example", "foo"), JSet.of(), JList.of(Path.of("com", "example", "bar"), Path.of("com", "example", "baz")))).asJava
  }

  it should "parse use" in {
    ModuleParser.parse("module foo { uses com.example.foo; }").getUses shouldEqual List(Path.of("com", "example", "foo")).asJava
  }

  it should "parse provides" in {
    ModuleParser.parse("module foo { provides com.example.foo with com.example.bar, com.example.baz; }").getProvides shouldEqual
      List(new Module.Provide(Path.of("com", "example", "foo"), JList.of(Path.of("com", "example", "bar"), Path.of("com", "example", "baz")))).asJava
  }

  it should "parse all kinds of generated textified values" in {
    generatorTest(CoreGenerator.module, ModuleParser, ModuleTextifier.getInstance())
  }
}
