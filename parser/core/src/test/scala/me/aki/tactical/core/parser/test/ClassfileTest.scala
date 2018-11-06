package me.aki.tactical.core.parser.test

import java.util.{Optional, Set => JSet}

import me.aki.tactical.core.Classfile.InnerClass
import me.aki.tactical.core.annotation.Annotation

import scala.collection.JavaConverters._
import me.aki.tactical.core._
import me.aki.tactical.core.`type`._
import me.aki.tactical.core.parser.ClassfileParser
import me.aki.tactical.core.typeannotation._
import me.aki.tactical.core.typeannotation.TargetType.Extends
import me.aki.tactical.core.typeannotation.TypePath.Kind
import org.scalatest.{FlatSpec, Matchers}

class ClassfileTest extends FlatSpec with Matchers {
  val classParser = new ClassfileParser(DummyBodyParser)

  "The classParser" should "parse the most basic classes" in {
    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {}
      """.stripMargin
    } shouldEqual new Classfile(new Classfile.Version(54, 0), Path.of("com", "example", "Foo"), null, Nil.asJava)
  }

  it should "parse implementes and extends" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo extends java.lang.Object implements java.lang.Cloneable {}
      """.stripMargin
    }
    classfile.getSupertype shouldEqual Path.of("java", "lang", "Object")
    classfile.getInterfaces shouldEqual List(Path.of("java", "lang", "Cloneable")).asJava
  }

  it should "parse different class keywords" in {
    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |interface Foo extends java.lang.Object {}
      """.stripMargin
    }.getFlag(Classfile.Flag.INTERFACE) shouldEqual true

    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |@interface Foo extends java.lang.Object {}
      """.stripMargin
    }.getFlag(Classfile.Flag.ANNOTATION) shouldEqual true

    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |enum Foo extends java.lang.Object {}
      """.stripMargin
    }.getFlag(Classfile.Flag.ENUM) shouldEqual true

    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |module Foo extends java.lang.Object {}
      """.stripMargin
    }.getFlag(Classfile.Flag.MODULE) shouldEqual true
  }

  it should "parse class signatures" in {
    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |signature "<T:Ljava/lang/Object;>Ljava/lang/Object;";
        |class Foo {}
      """.stripMargin
    }.getSignature shouldEqual Optional.of("<T:Ljava/lang/Object;>Ljava/lang/Object;")
  }

  it should "parse source and debug info" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |source "Foo.java";
        |debug "bar";
        |class Foo {}
      """.stripMargin
    }

    classfile.getSource shouldEqual Optional.of("Foo.java")
    classfile.getSourceDebug shouldEqual Optional.of("bar")
  }

  it should "parse annotations" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |@java.lang.Deprecated[visible = true]();
        |class Foo {}
      """.stripMargin
    }

    classfile.getAnnotations shouldEqual List(new Annotation(Path.of("java", "lang", "Deprecated"), true)).asJava
  }

  it should "parse type annotations" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |#[path = { ? <1> }, target = extends, annotation = @java.lang.Deprecated[visible = true]()];
        |class Foo {}
      """.stripMargin
    }

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Deprecated"), true)
      new ClassTypeAnnotation(typePath, annotation, new Extends())
    }

    classfile.getTypeAnnotations shouldEqual List(typeAnnotation).asJava
  }

  it should "parse attributes" in {
    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |attribute "foo" { 00 0F 00 FF }
        |class Foo {
        |    module com.example.Foo {}
        |}
      """.stripMargin
    }.getAttributes shouldEqual List(new Attribute("foo", Array(0, 15, 0, -1))).asJava
  }

  it should "parse a module" in {
    classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |    module com.example.Foo {}
        |}
      """.stripMargin
    }.getModule shouldEqual Optional.of(new Module(Path.of("com", "example", "Foo")))
  }

  it should "parse the enclosing method" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |    enclosing com.example.Bar {
        |        name = "baz";
        |        descriptor = (int, long) void;
        |    }
        |}
      """.stripMargin
    }

    val descriptor = new MethodDescriptor(List[Type](IntType.getInstance, LongType.getInstance).asJava, Optional.empty())
    classfile.getEnclosingMethod shouldEqual Optional.of(new Classfile.EnclosingMethod(Path.of("com", "example", "Bar"), Optional.of("baz"), Optional.of(descriptor)))
  }

  it should "parse inner classes" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |    public static final inner com.example.Bar$Baz {
        |        inner "Baz";
        |        outer com.example.Bar;
        |    }
        |}
      """.stripMargin
    }

    val innerClass = {
      val flags = JSet.of[InnerClass.Flag](InnerClass.Flag.PUBLIC, InnerClass.Flag.FINAL, InnerClass.Flag.STATIC)
      new InnerClass(Path.of("com", "example", "Bar$Baz"), Optional.of(Path.of("com", "example", "Bar")), Optional.of("Baz"), flags)
    }
    classfile.getInnerClasses shouldEqual List(innerClass).asJava
  }

  it should "parse the nest host" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |    nest host com.example.Bar;
        |}
      """.stripMargin
    }
    classfile.getNestHost shouldEqual Optional.of(Path.of("com", "example", "Bar"))
  }

  it should "parse the nest members" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |    nest member com.example.Bar, com.example.Baz;
        |}
      """.stripMargin
    }
    classfile.getNestMembers shouldEqual List(Path.of("com", "example", "Bar"), Path.of("com", "example", "Baz")).asJava
  }

  it should "parse fields" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |  int i;
        |}
      """.stripMargin
    }
    classfile.getFields shouldEqual List(new Field("i", IntType.getInstance)).asJava
  }

  it should "parse methods" in {
    val classfile = classParser.parse {
      """package com.example;
        |version 54.0;
        |
        |class Foo {
        |  void foo();
        |}
      """.stripMargin
    }
    classfile.getMethods shouldEqual List(new Method("foo", Nil.asJava, Optional.empty[Type])).asJava
  }
}