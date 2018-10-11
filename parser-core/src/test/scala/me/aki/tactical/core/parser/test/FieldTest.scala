package me.aki.tactical.core.parser.test

import scala.collection.JavaConverters._
import java.util.{Optional, Set => JSet}

import me.aki.tactical.core.{Attribute, Field, Path}
import me.aki.tactical.core.`type`.IntType
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.constant.IntConstant
import me.aki.tactical.core.parser.FieldParser
import me.aki.tactical.core.typeannotation.TargetType.Extends
import me.aki.tactical.core.typeannotation.TypePath.Kind
import me.aki.tactical.core.typeannotation.{ClassTypeAnnotation, FieldTypeAnnotation, TypePath}
import org.scalatest.{FlatSpec, Matchers}

class FieldTest extends FlatSpec with Matchers {
  "The FieldParser" should "parse the most basic field" in {
    FieldParser.parse("int foo;") shouldEqual new Field("foo", IntType.getInstance)
  }

  it should "parse flags" in {
    FieldParser.parse("private static final int foo;").getFlags shouldEqual
      JSet.of(Field.Flag.PRIVATE, Field.Flag.FINAL, Field.Flag.STATIC)
  }

  it should "parse values of constants" in {
    FieldParser.parse("static int foo = 20;").getValue shouldEqual
      Optional.of(new IntConstant(20))
  }

  it should "parse the signature" in {
    FieldParser.parse(
      """signature "I";
        |int foo = 20;
      """.stripMargin
    ).getSignature shouldEqual Optional.of("I")
  }

  it should "parse annotations" in {
    FieldParser.parse(
      """@java.lang.Override[visible = true]()
        |int foo = 20;
      """.stripMargin
    ).getAnnotations shouldEqual List(new Annotation(Path.of("java", "lang", "Override"), true)).asJava
  }

  it should "parse type annotations" in {
    val field = FieldParser.parse(
      """#[path = { ? <1> }, annotation = @java.lang.Deprecated[visible = true]()];
        |int foo = 20;
      """.stripMargin
    )

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), true)
      new FieldTypeAnnotation(typePath, annotation)
    }

    field.getTypeAnnotations shouldEqual List(typeAnnotation).asJava
  }

  it should "parse attributes" in {
    FieldParser.parse(
      """attribute "foo" { 00 FF }
        |int foo = 20;
      """.stripMargin
    ).getAttributes shouldEqual List(new Attribute("foo", Array(0, -1))).asJava
  }
}
