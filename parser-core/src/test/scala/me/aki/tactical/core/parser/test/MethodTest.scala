package me.aki.tactical.core.parser.test

import scala.collection.JavaConverters._
import java.util.{ArrayList, Optional, Set => JSet}

import me.aki.tactical.core.Classfile.Version
import me.aki.tactical.core._
import me.aki.tactical.core.`type`.{IntType, ObjectType}
import me.aki.tactical.core.annotation.{Annotation, IntAnnotationValue}
import me.aki.tactical.core.parser.MethodParser
import me.aki.tactical.core.textify.MethodTextifier
import me.aki.tactical.core.typeannotation.{MethodTypeAnnotation, TargetType, TypePath}
import me.aki.tactical.core.typeannotation.TypePath.Kind
import org.scalatest.{FlatSpec, Matchers}

class MethodTest extends FlatSpec with Matchers {
  val dummyClass = new Classfile(new Version(Version.MAJOR_JDK_8, 0), Path.of("foo", "Dummy"), Path.OBJECT, new ArrayList())
  object DummyMethodParser extends MethodParser(dummyClass, DummyBodyParser)

  "The MethodParser" should "parse the most basic method" in {
    DummyMethodParser.parse("void a();") shouldEqual
      new Method("a", Nil.asJava, Optional.empty())
  }

  it should "parse a basic constructor" in {
    val className = dummyClass.getName.getName
    val method = DummyMethodParser.parse(s"$className() {}")
    method.getName shouldEqual "<init>"
  }

  it should "parse a basic static initializer" in {
    val method = DummyMethodParser.parse(s"static {}")
    method.getName shouldEqual "<clinit>"
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

  it should "parse return types" in {
    val method = DummyMethodParser.parse("int value();")

    method.getReturnType shouldEqual Optional.of(IntType.getInstance)
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
        |void a();
      """.stripMargin
    )

    val flags = JSet.of(Method.Parameter.Flag.FINAL, Method.Parameter.Flag.SYNTHETIC)
    method.getParameterInfo shouldEqual List(new Method.Parameter(Optional.of("foo"), flags)).asJava
  }

  it should "parse method parameter annotations" in {
    val method = DummyMethodParser.parse(
      """parameter annotations {}
        |parameter annotations { @java.lang.Override[visible = true]() }
        |void a();
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
        |void a();
      """.stripMargin
    )

    method.getAnnotations shouldEqual List(new Annotation(Path.of("java", "lang", "Override"), true)).asJava
  }

  it should "parse type annotations" in {
    val method = DummyMethodParser.parse(
      """#[path = { ? <1> }, target = exception 0, annotation = @java.lang.Deprecated[visible = true]()];
        |void a();
      """.stripMargin
    )

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Deprecated"), true)
      val target = new TargetType.CheckedException(0)
      new MethodTypeAnnotation(typePath, annotation, target)
    }

    method.getTypeAnnotations shouldEqual List(typeAnnotation).asJava
  }

  it should "parse attributes" in {
    DummyMethodParser.parse(
      """attribute "foo" { 10 FF }
        |void a();
      """.stripMargin
    ).getAttributes shouldEqual List(new Attribute("foo", Array(16, -1))).asJava
  }

  ignore should "parse random textified methods" in {
    generatorTest(CoreGenerator.method, DummyMethodParser, new MethodTextifier(null, dummyClass))
  }
}
