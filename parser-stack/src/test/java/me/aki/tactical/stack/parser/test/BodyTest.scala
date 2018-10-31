package me.aki.tactical.stack.parser.test

import scala.collection.JavaConverters._
import java.util.{ArrayList, Optional}

import me.aki.tactical.core.{Classfile, Path}
import me.aki.tactical.core.Classfile.Version
import me.aki.tactical.core.`type`.{IntType, ObjectType}
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.parser.MethodParser
import me.aki.tactical.core.typeannotation.TargetType.{New, ResourceVariable}
import me.aki.tactical.core.typeannotation.TypePath.Kind
import me.aki.tactical.core.typeannotation.{InsnTypeAnnotation, LocalVariableTypeAnnotation, TypePath}
import me.aki.tactical.stack.StackBody.{LineNumber, LocalVariable, LocalVariableAnnotation}
import me.aki.tactical.stack.{StackBody, TryCatchBlock}
import me.aki.tactical.stack.insn._
import me.aki.tactical.stack.parser.StackBodyParser
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class BodyTest extends FlatSpec with Matchers with PropertyChecks {
  val dummyClass = new Classfile(new Version(Version.MAJOR_JDK_8, 0), Path.of("foo", "Dummy"), Path.OBJECT, new ArrayList())
  val methodParser = new MethodParser(dummyClass, StackBodyParser)

  "The StackBodyParser" should "parse instructions" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  pop;
        |  swap;
        |  return;
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    body.getInstructions shouldEqual List(new PopInsn, new SwapInsn, new ReturnInsn).asJava
  }

  it should "parse labels" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |    goto label1;
        |
        |  label1:
        |    return;
        |}
      """.stripMargin.trim)

    val insns = method.getBody.get().asInstanceOf[StackBody].getInstructions
    insns.get(0).asInstanceOf[GotoInsn].getTarget shouldEqual insns.get(1)
  }

  it should "parse method parameters" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {}
      """.stripMargin.trim)

    method.getParameterTypes shouldEqual List(ObjectType.STRING, IntType.getInstance).asJava
  }

  it should "parse uses of the parameter locals" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  load ref param1;
        |  load int param2;
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    body.getInstructions.get(0).asInstanceOf[LoadInsn].getLocal shouldEqual body.getParameterLocals.get(0)
    body.getInstructions.get(1).asInstanceOf[LoadInsn].getLocal shouldEqual body.getParameterLocals.get(1)
  }

  it should "parse uses of the this locals" in {
    val method = methodParser.parse(
      """void a() {
        |  load ref this;
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    body.getInstructions.get(0).asInstanceOf[LoadInsn].getLocal shouldEqual body.getThisLocal.get
  }

  it should "parse try/catch blocks" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  startLabel:
        |    push "foo";
        |
        |  endLabel:
        |    return;
        |
        |  handlerLabel:
        |    pop;
        |
        |    try startLabel -> endLabel catch handlerLabel : java.lang.Exception;
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    val insns = body.getInstructions
    body.getTryCatchBlocks shouldEqual List(new TryCatchBlock(insns.get(0), insns.get(1), insns.get(2), Optional.of(Path.of("java", "lang", "Exception")))).asJava
  }

  it should "parse line numbers" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  label1:
        |    return;
        |
        |    line 1 label1;
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    val insns = body.getInstructions
    body.getLineNumbers shouldEqual List(new LineNumber(1, insns.get(0))).asJava
  }

  it should "parse local variables infos" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  start:
        |    load ref param1;
        |
        |  end:
        |    return ref;
        |
        |    local info start -> end param2 "foo" int;
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    val insns = body.getInstructions
    body.getLocalVariables shouldEqual List(new LocalVariable("foo", IntType.getInstance, Optional.empty[String], insns.get(0), insns.get(1), body.getParameterLocals.get(1))).asJava
  }

  it should "parse local variables annotations" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  start:
        |    load ref param1;
        |
        |  end:
        |    return ref;
        |
        |    local annotation [start -> end param1] #[path = { ? <1> }, target = resource, annotation = @java.lang.Override[visible = false]()];
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    val insns = body.getInstructions

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Override"), false)
      new LocalVariableTypeAnnotation(typePath, annotation, new ResourceVariable())
    }

    val locations = List(new StackBody.LocalVariableAnnotation.Location(insns.get(0), insns.get(1), body.getParameterLocals.get(0))).asJava

    body.getLocalVariableAnnotations shouldEqual List(new LocalVariableAnnotation(typeAnnotation, locations)).asJava
  }

  it should "parse instruction annotations" in {
    val method = methodParser.parse(
      """void a(java.lang.String param1, int param2) {
        |  label1:
        |    load ref param1;
        |    return ref;
        |
        |    insn annotation label1 #[path = { ? <1> }, target = new, annotation = @java.lang.Override[visible = false]()];
        |}
      """.stripMargin.trim)

    val body = method.getBody.get().asInstanceOf[StackBody]
    val annotatedInsn = body.getInstructions.get(0)

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Override"), false)
      new InsnTypeAnnotation(typePath, annotation, new New())
    }

    annotatedInsn.getTypeAnnotations shouldEqual List(typeAnnotation).asJava
  }
}
