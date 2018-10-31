package me.aki.tactical.stack.parser.test

import scala.collection.JavaConverters._
import java.util.Optional

import me.aki.tactical.core.Path
import me.aki.tactical.core.`type`.{IntType, LongType}
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.typeannotation.TargetType.{New, ResourceVariable}
import me.aki.tactical.core.typeannotation.TypePath.Kind
import me.aki.tactical.core.typeannotation.{InsnTypeAnnotation, LocalVariableTypeAnnotation, TypePath}
import me.aki.tactical.stack.{StackBody, StackLocal, TryCatchBlock}
import me.aki.tactical.stack.insn.{Instruction, PopInsn}
import me.aki.tactical.stack.parser._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ContentTest extends FlatSpec with Matchers with PropertyChecks {
  val local1 = new StackLocal
  val local2 = new StackLocal
  val local3 = new StackLocal
  val local4 = new StackLocal

  val label1: Instruction = new PopInsn()
  val label2: Instruction = new PopInsn()
  val label3: Instruction = new PopInsn()
  val label4: Instruction = new PopInsn()

  val newCtx = new ResolvedStackCtx(Map(
    "local1" -> local1,
    "local2" -> local2,
    "local3" -> local3,
    "local4" -> local4
  ), Map(
    "label1" -> label1,
    "label2" -> label2,
    "label3" -> label3,
    "label4" -> label4
  ))

  "The TryCatchBlockParser" should "parse try catch blocks without exception types" in {
    new TryCatchBlockParser(newCtx).parse("try label1 -> label2 catch label3;") shouldEqual
      new TryCatchBlock(label1, label2, label3, Optional.empty())
  }

  it should "parse try catch blocks with exception types" in {
    new TryCatchBlockParser(newCtx).parse("try label1 -> label2 catch label3 : java.lang.Exception;")
      new TryCatchBlock(label1, label2, label3, Optional.of(Path.of("java", "lang", "Exception")))
  }

  "The LineNumberParser" should "parse line number declarations" in {
    forAll { line: Int =>
      new LineNumberParser(newCtx).parse(s"line $line label1;") shouldEqual
        new StackBody.LineNumber(line, label1)
    }
  }

  "The LocalInfoParser" should "parse local variable infos" in {
    new LocalVariableParser(newCtx).parse("local info label1 -> label2 local1 \"foo\" int;") shouldEqual
      new StackBody.LocalVariable("foo", IntType.getInstance, Optional.empty[String], label1, label2, local1)
  }

  it should "parse local variable infos with signature" in {
    new LocalVariableParser(newCtx).parse("local info label1 -> label2 local1 \"foo\" long \"J\";") shouldEqual
      new StackBody.LocalVariable("foo", LongType.getInstance, Optional.of("J"), label1, label2, local1)
  }

  "The LocalVariableAnnotationParser" should "parse local variable type annotations" in {
    val anno = new LocalVariableAnnotationParser(newCtx).parse(
      """local annotation [label1 -> label2 local1, label3 -> label1 local2] #[path = { ? <1> }, target = resource, annotation = @java.lang.Override[visible = false]()];""")

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Override"), false)
      new LocalVariableTypeAnnotation(typePath, annotation, new ResourceVariable())
    }

    val locations = List[StackBody.LocalVariableAnnotation.Location] (
      new StackBody.LocalVariableAnnotation.Location(label1, label2, local1),
      new StackBody.LocalVariableAnnotation.Location(label3, label1, local2)
    ).asJava

    anno shouldEqual new StackBody.LocalVariableAnnotation(typeAnnotation, locations)
  }

  "The InsnAnnotationParser" should "parse instruction annotation" in {
    val (label, annotation) = new InsnAnnotationParser(newCtx).parse(
      "insn annotation label1 #[path = { ? <1> }, target = new, annotation = @java.lang.Override[visible = false]()];")

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Override"), false)
      new InsnTypeAnnotation(typePath, annotation, new New())
    }

    label shouldEqual label1
    typeAnnotation shouldEqual typeAnnotation
  }
}
