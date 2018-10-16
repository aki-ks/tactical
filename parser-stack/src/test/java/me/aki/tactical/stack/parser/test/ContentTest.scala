package me.aki.tactical.stack.parser.test

import scala.collection.JavaConverters._
import java.util.Optional

import me.aki.tactical.core.Path
import me.aki.tactical.core.`type`.{IntType, LongType}
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.typeannotation.TargetType.{LocalVariable, ResourceVariable}
import me.aki.tactical.core.typeannotation.TypePath.Kind
import me.aki.tactical.core.typeannotation.{LocalVariableTypeAnnotation, TypePath}
import me.aki.tactical.stack.parser._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ContentTest extends FlatSpec with Matchers with PropertyChecks with StackCtxTest {
  "The TryCatchBlockParser" should "parse try catch blocks without exception types" in {
    val ctx = newCtx
    val block = new TryCatchBlockParser(ctx).parse("try label1 -> label2 catch label3;")

    block.getExceptionType shouldEqual Optional.empty()
    validateLabels(ctx, Map(
      "label1" -> block.getFirstCell,
      "label2" -> block.getLastCell,
      "label3" -> block.getHandlerCell
    ))
  }

  it should "parse try catch blocks with exception types" in {
    val ctx = newCtx
    val block = new TryCatchBlockParser(ctx).parse("try label1 -> label2 catch label3 : java.lang.Exception;")

    block.getExceptionType shouldEqual Optional.of(Path.of("java", "lang", "Exception"))
    validateLabels(ctx, Map(
      "label1" -> block.getFirstCell,
      "label2" -> block.getLastCell,
      "label3" -> block.getHandlerCell
    ))
  }

  "The LineNumberParser" should "parse line number declarations" in {
    forAll { line: Int =>
      val ctx = newCtx

      val node = new LineNumberParser(ctx).parse(s"line $line label1;")

      node.getLine shouldEqual line
      validateLabels(ctx, Map("label1" -> node.getInstructionCell))
    }
  }

  "The LocalInfoParser" should "parse local variable infos" in {
    val ctx = newCtx
    val local = new LocalVariableParser(ctx).parse("local info label1 -> label2, local1, \"foo\", int;")

    local.getName shouldEqual "foo"
    local.getLocal shouldEqual local1
    local.getType shouldEqual IntType.getInstance
    local.getSignature shouldEqual Optional.empty
    validateLabels(ctx, Map(
      "label1" -> local.getStartCell,
      "label2" -> local.getEndCell
    ))
  }

  it should "parse local variable infos with signature" in {
    val ctx = newCtx
    val local = new LocalVariableParser(ctx).parse("local info label1 -> label2, local1, \"foo\", long, \"J\";")

    local.getName shouldEqual "foo"
    local.getLocal shouldEqual local1
    local.getType shouldEqual LongType.getInstance
    local.getSignature shouldEqual Optional.of("J")
    validateLabels(ctx, Map(
      "label1" -> local.getStartCell,
      "label2" -> local.getEndCell
    ))
  }

  "The LocalVariableAnnotationParser" should "parse local variable type annotations" in {
    val ctx = newCtx
    val anno = new LocalVariableAnnotationParser(ctx).parse(
      """local annotation [label1 -> label2 local1, label3 -> label1 local2] #[path = { ? <1> }, target = resource, annotation = @java.lang.Override[visible = false]()]""")

    val typeAnnotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), false)
      new LocalVariableTypeAnnotation(typePath, annotation, new ResourceVariable())
    }
    anno.getAnnotation shouldEqual typeAnnotation

    val location1 = anno.getLocations.get(0)
    val location2 = anno.getLocations.get(1)

    location1.getLocal shouldEqual local1
    location2.getLocal shouldEqual local2

    validateLabels(ctx, Map(
      "label1" -> location1.getStartCell,
      "label2" -> location1.getEndCell,
      "label3" -> location2.getStartCell,
      "label1" -> location2.getEndCell
    ))
  }

}
