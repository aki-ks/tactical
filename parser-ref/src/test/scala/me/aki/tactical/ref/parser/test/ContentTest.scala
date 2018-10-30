package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._
import java.util.Optional

import me.aki.tactical.core.Path
import me.aki.tactical.core.`type`.IntType
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.typeannotation.TargetType.LocalVariable
import me.aki.tactical.core.typeannotation.{LocalVariableTypeAnnotation, TypePath}
import me.aki.tactical.core.typeannotation.TypePath.Kind
import me.aki.tactical.ref.{RefBody, TryCatchBlock}
import me.aki.tactical.ref.parser._

class ContentTest extends AbstractResolvedCtxTest {
  "The TryCatchBlockParser" should "parse try catch blocks" in {
    new TryCatchBlockParser(parseCtx).parse("try label1 -> label2 catch label3 local1;") shouldEqual
      new TryCatchBlock(label1, label2, label3, Optional.empty(), local1)

    new TryCatchBlockParser(parseCtx).parse("try label1 → label2 catch label3 local1;") shouldEqual
      new TryCatchBlock(label1, label2, label3, Optional.empty(), local1)

    new TryCatchBlockParser(parseCtx).parse("try label1 -> label2 catch label3 local1 : java.lang.Exception;") shouldEqual
      new TryCatchBlock(label1, label2, label3, Optional.of(Path.of("java", "lang", "Exception")), local1)
  }

  "The LineParser" should "parse line numbers" in {
    new LineParser(parseCtx).parse("line 20 label1;") shouldEqual new RefBody.LineNumber(20, label1)
  }

  "The LocalVariableParser" should "parse local variables" in {
    new LocalVariableParser(parseCtx).parse("local info label1 -> label2 local1 \"foo\" int;") shouldEqual
      new RefBody.LocalVariable("foo", IntType.getInstance, Optional.empty[String], label1, label2, local1)

    new LocalVariableParser(parseCtx).parse("local info label1 -> label2 local1 \"foo\" int \"I\";") shouldEqual
      new RefBody.LocalVariable("foo", IntType.getInstance, Optional.of("I"), label1, label2, local1)
  }

  "The LocalVariableAnnotationParser" should "parse local variable annotations" in {
    val annotationString = "#[path = { ? <1> }, target = local, annotation = @java.lang.Override[visible = false]()]"
    val annotation = {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Override"), false)
      new LocalVariableTypeAnnotation(typePath, annotation, new LocalVariable())
    }

    val locations = new RefBody.LocalVariableAnnotation.Location(label1, label2, local1) ::
      new RefBody.LocalVariableAnnotation.Location(label2, label3, local2) :: Nil

    new LocalVariableAnnotationParser(parseCtx).parse(s"local annotation [label1 -> label2 local1, label2 -> label3 local2] $annotationString") shouldEqual
      new RefBody.LocalVariableAnnotation(annotation, locations.asJava)
  }
}
