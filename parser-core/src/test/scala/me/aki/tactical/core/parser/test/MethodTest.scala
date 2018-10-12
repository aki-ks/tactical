package me.aki.tactical.core.parser.test

import scala.collection.JavaConverters._
import java.util.{Optional, Set => JSet}

import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.`type`.{IntType, ObjectType}
import me.aki.tactical.core.annotation.{Annotation, IntAnnotationValue}
import me.aki.tactical.core.parser.{MethodParser, Parser}
import me.aki.tactical.core.typeannotation.{FieldTypeAnnotation, MethodTypeAnnotation, TargetType, TypePath}
import me.aki.tactical.core.typeannotation.TypePath.Kind
import org.scalatest.{FlatSpec, Matchers}

class MethodTest extends FlatSpec with Matchers {
  object DummyBodyParser extends Parser[Body] {
    val parser: P[Body] = for (_ ← Pass) yield new Body {}
  }

  object DummyMethodParser extends MethodParser(DummyBodyParser)

  "The MethodParser" should "parse the most basic method" in {
    DummyMethodParser.parse("void a();") shouldEqual
      new Method("a", Nil.asJava, Optional.empty())
  }

  it should "parse abstract methods" in {
    DummyMethodParser.parse("void a();").getBody shouldEqual Optional.empty()
  }

  it should "parse non-abstract methods" in {
    DummyMethodParser.parse("void a() {}").getBody.isPresent shouldEqual true
  }

  it should "parse default annotation values" in {
    DummyMethodParser.parse("int value() = 20;").getDefaultValue shouldEqual
      Optional.of(new IntAnnotationValue(20))
  }

  it should "parse parameter and return types" in {
    val method = DummyMethodParser.parse("int value(java.lang.String, int);")

    method.getReturnType shouldEqual Optional.of(IntType.getInstance)
    method.getParameterTypes shouldEqual List(new ObjectType(Path.of("java", "lang", "String")), IntType.getInstance).asJava
  }

  it should "parse method flags" in {
    DummyMethodParser.parse("private final static void a() {}").getFlags shouldEqual
      JSet.of(Method.Flag.PRIVATE, Method.Flag.FINAL, Method.Flag.STATIC)
  }

  it should "parse checked exceptions" in {
    DummyMethodParser.parse("void a() throws java.lang.RuntimeException, java.lang.IllegalArgumentException;").getExceptions shouldEqual
      List(Path.of("java", "lang", "RuntimeException"), Path.of("java", "lang", "IllegalArgumentException")).asJava
  }

  it should "parse method signatures" in {
    DummyMethodParser.parse(
      """signature "<T:Ljava/lang/Object;>()TT;";
        |void a();
      """.stripMargin
    ).getSignature shouldEqual Optional.ofNullable("<T:Ljava/lang/Object;>()TT;")
  }

  it should "parse method parameter info" in {
    val method = DummyMethodParser.parse(
      """final synthetic parameter "foo";
        |void a(int);
      """.stripMargin
    )

    val flags = JSet.of(Method.Parameter.Flag.FINAL, Method.Parameter.Flag.SYNTHETIC)
    method.getParameterInfo shouldEqual List(new Method.Parameter(Optional.of("foo"), flags)).asJava
  }

  it should "parse method parameter annotations" in {
    val method = DummyMethodParser.parse(
      """parameter annotations {}
        |parameter annotations { @java.lang.Override[visible = true]() }
        |void a(int, long);
      """.stripMargin
    )

    val flags = JSet.of(Method.Parameter.Flag.FINAL, Method.Parameter.Flag.SYNTHETIC)
    method.getParameterAnnotations shouldEqual List(
      List().asJava,
      List(new Annotation(Path.of("java", "lang", "Override"), true)).asJava
    ).asJava
  }

  it should "parse annotations" in {
    val method = DummyMethodParser.parse(
      """@java.lang.Override[visible = true]()
        |void a(int, long);
      """.stripMargin
    )

    method.getAnnotations shouldEqual List(new Annotation(Path.of("java", "lang", "Override"), true)).asJava
  }

  it should "parse type annotations" in {
    val method = DummyMethodParser.parse(
      """#[path = { ? <1> }, target = exception 0, annotation = @java.lang.Deprecated[visible = true]()];
        |void a(int, long);
      """.stripMargin
    )

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), true)
      val target = new TargetType.CheckedException(0)
      new MethodTypeAnnotation(typePath, annotation, target)
    }

    method.getTypeAnnotations shouldEqual List(typeAnnotation).asJava
  }

  it should "parse attributes" in {
    DummyMethodParser.parse(
      """attribute "foo" { 10 FF }
        |void a(int, long);
      """.stripMargin
    ).getAttributes shouldEqual List(new Attribute("foo", Array(16, -1))).asJava
  }
}