package me.aki.tactical.core.parser.test

import scala.collection.JavaConverters._
import me.aki.tactical.core.parser.{TypePathKindParser, TypePathParser}
import me.aki.tactical.core.typeannotation.TypePath
import me.aki.tactical.core.typeannotation.TypePath.Kind
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class TypeAnnotationTest extends FlatSpec with Matchers with PropertyChecks {
  "The TypePathKindParser" should "parse all type path kinds" in {
    TypePathKindParser.parse("[]") shouldEqual new Kind.Array()
    TypePathKindParser.parse(".") shouldEqual new Kind.InnerClass()
    TypePathKindParser.parse("?") shouldEqual new Kind.WildcardBound()
    TypePathKindParser.parse("<0>") shouldEqual new Kind.TypeArgument(0)
    TypePathKindParser.parse("<1>") shouldEqual new Kind.TypeArgument(1)
  }

  "The TypePathParser" should "parse TypePaths" in {
    TypePathParser.parse("{}") shouldEqual new TypePath(Nil.asJava)
    TypePathParser.parse("{ [] ? <3> }") shouldEqual
      new TypePath(List[Kind](new Kind.Array(), new Kind.WildcardBound(), new Kind.TypeArgument(3)).asJava)
  }
}
