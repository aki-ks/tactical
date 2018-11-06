package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._
import java.util.{ArrayList, Optional}

import me.aki.tactical.core.{Classfile, Path}
import me.aki.tactical.core.Classfile.Version
import me.aki.tactical.core.`type`.{IntType, ObjectType}
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.parser.MethodParser
import me.aki.tactical.core.typeannotation.TargetType.LocalVariable
import me.aki.tactical.core.typeannotation.TypePath.Kind
import me.aki.tactical.core.typeannotation.{LocalVariableTypeAnnotation, TypePath}
import me.aki.tactical.ref.RefBody.LineNumber
import me.aki.tactical.ref.{RefBody, TryCatchBlock}
import me.aki.tactical.ref.expr.AddExpr
import me.aki.tactical.ref.parser.RefBodyParser
import me.aki.tactical.ref.stmt.ReturnStmt
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class BodyTest extends FlatSpec with Matchers with PropertyChecks {
  val dummyClass = new Classfile(new Version(Version.MAJOR_JDK_8, 0), Path.of("foo", "Dummy"), Path.OBJECT, new ArrayList())
  val methodParser = new MethodParser(dummyClass, RefBodyParser)

  def parse(methodString: String) =
    methodParser.parse(methodString).getBody.get.asInstanceOf[RefBody]

  "The RefBodyParser" should "the most basic static body" in {
    val body = parse("static void a() {}")
    body.getLocals.size shouldEqual 0
    body.getThisLocal shouldEqual Optional.empty()
  }

  it should "the most basic non-static body" in {
    val body = parse("void a() {}")
    body.getLocals.size shouldEqual 1
    body.getThisLocal.get shouldEqual body.getLocals.get(0)
  }

  it should "a basic methods with parameters" in {
    val method = methodParser.parse("void a(int param1, java.lang.String l1) {}")
    method.getParameterTypes shouldEqual List(IntType.getInstance, ObjectType.STRING).asJava

    val body = method.getBody.get.asInstanceOf[RefBody]
    body.getLocals.size shouldEqual 1 + 2
    body.getThisLocal.get shouldEqual body.getLocals.get(0)
    body.getArgumentLocals.size shouldEqual 2
  }

  it should "parse method with statements" in {
    val body = parse(
      """void a() {
        |  return;
        |}
      """.stripMargin.trim)

    body.getStatements.asScala match {
      case Seq(stmt: ReturnStmt) => stmt.getValue shouldEqual Optional.empty
    }
  }

  it should "parse method referencing the this local" in {
    val body = parse(
      """void a() {
        |  return this;
        |}
      """.stripMargin.trim)

    body.getStatements.asScala match {
      case Seq(stmt: ReturnStmt) =>
        stmt.getValue shouldEqual body.getThisLocal
    }
  }

  it should "parse method referencing parameter locals" in {
    val body = parse(
      """void a(int param1, java.lang.String param2) {
        |  return param1;
        |  return param2;
        |}
      """.stripMargin.trim)

    body.getStatements.asScala match {
      case Seq(stmt1: ReturnStmt, stmt2: ReturnStmt) =>
        stmt1.getValue shouldEqual Optional.of(body.getArgumentLocals.get(0))
        stmt2.getValue shouldEqual Optional.of(body.getArgumentLocals.get(1))
    }
  }

  it should "parse methods with custom locals" in {
    val body = parse(
      """static int a() {
        |  local int foo, bar;
        |
        |  return foo + bar;
        |}
      """.stripMargin.trim)

    body.getStatements.asScala match {
      case Seq(stmt: ReturnStmt) =>
        stmt.getValue shouldEqual Optional.of(new AddExpr(body.getLocals.get(0), body.getLocals.get(1)))
    }
  }

  it should "parse methods with try/catch blocks" in {
    val body = parse(
      """void a() {
        |  local java.lang.Throwable e1;
        |
        |label1:
        |  return;
        |label2:
        |  return;
        |label3:
        |  return;
        |
        |  try label1 -> label2 catch label3 e1;
        |}
      """.stripMargin.trim)

    val stmts = body.getStatements
    body.getTryCatchBlocks.asScala shouldEqual List(
      new TryCatchBlock(stmts.get(0), stmts.get(1), stmts.get(2), Optional.empty(), body.getLocals.get(1))
    )
  }

  it should "parse methods with line numbers" in {
    val body = parse(
      """void a() {
        |label1:
        |  return;
        |
        |  line 20 label1;
        |}
      """.stripMargin.trim)

    body.getLineNumbers.asScala shouldEqual List(
      new LineNumber(20, body.getStatements.get(0))
    )
  }

  it should "parse methods with local variable" in {
    val body = parse(
      """int a(int param1, int param2) {
        |label1:
        |  param1 = param1 + param2;
        |
        |label2:
        |  return param1;
        |
        |  local info label1 -> label2 param1 "a" int "I";
        |}
      """.stripMargin.trim)

    val stmts = body.getStatements
    body.getLocalVariables.asScala shouldEqual List(
      new RefBody.LocalVariable("a", IntType.getInstance, Optional.of("I"), stmts.get(0), stmts.get(1), body.getArgumentLocals.get(0))
    )
  }

  it should "parse methods with local variable annotations" in {
    val annotationString = "#[path = { ? <1> }, target = local, annotation = @java.lang.Override[visible = false]()]"
    val annotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Override"), false)
      new LocalVariableTypeAnnotation(typePath, annotation, new LocalVariable())
    }

    val body = parse(s"""
        |int a(int param1, int param2) {
        |label1:
        |  param1 = param1 + param2;
        |
        |label2:
        |  return param1;
        |
        |  local annotation [label1 -> label2 param1] $annotationString;
        |}
      """.stripMargin.trim)

    val locations = List(new RefBody.LocalVariableAnnotation.Location(
      body.getStatements.get(0), body.getStatements.get(1), body.getArgumentLocals.get(0)))

    body.getLocalVariableAnnotations.asScala shouldEqual List(
      new RefBody.LocalVariableAnnotation(annotation, locations.asJava)
    )
  }
}
