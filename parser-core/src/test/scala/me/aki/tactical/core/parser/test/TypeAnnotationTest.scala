package me.aki.tactical.core.parser.test

import me.aki.tactical.core.Path
import me.aki.tactical.core.annotation.Annotation

import scala.collection.JavaConverters._
import me.aki.tactical.core.parser._
import me.aki.tactical.core.typeannotation._
import me.aki.tactical.core.typeannotation.TargetType._
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

  "The MethodTargetTypeParser" should "parse all kinds of MethodTargetTypes" in {
    forAll { exceptionIndex: Int =>
      MethodTargetTypeParser.parse(s"exception $exceptionIndex") shouldEqual new CheckedException(exceptionIndex)
    }

    forAll { parameterIndex: Int =>
      MethodTargetTypeParser.parse(s"parameter $parameterIndex") shouldEqual new MethodParameter(parameterIndex)
    }

    MethodTargetTypeParser.parse("receiver") shouldEqual new MethodReceiver()

    MethodTargetTypeParser.parse("return") shouldEqual new ReturnType()

    forAll { parameter: Int =>
      MethodTargetTypeParser.parse(s"type parameter $parameter") shouldEqual new TypeParameter(parameter)
    }

    forAll { (parameter: Int, bound: Int) =>
      MethodTargetTypeParser.parse(s"type parameter bound $parameter $bound") shouldEqual new TypeParameterBound(parameter, bound)
    }
  }

  "The ClassTargetTypeParser" should "parse all kinds of ClassTargetTypeParser" in {
    ClassTargetTypeParser.parse("extends") shouldEqual new Extends()

    forAll { interface: Int =>
      ClassTargetTypeParser.parse(s"implements $interface") shouldEqual new Implements(interface)
    }

    forAll { parameter: Int =>
      ClassTargetTypeParser.parse(s"type parameter $parameter") shouldEqual new TypeParameter(parameter)
    }

    forAll { (parameter: Int, bound: Int) =>
      ClassTargetTypeParser.parse(s"type parameter bound $parameter $bound") shouldEqual new TypeParameterBound(parameter, bound)
    }
  }

  "The ClassTypeAnnotationParser" should "parse classfile type annotations" in {
    ClassTypeAnnotationParser.parse("#[path = { ? <1> }, target = extends, annotation = @java.lang.Override[visible = true]()]") shouldEqual {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), true)
      new ClassTypeAnnotation(typePath, annotation, new Extends())
    }
  }

  "The MethodTypeAnnotationParser" should "parse method type annotations" in {
    MethodTypeAnnotationParser.parse("#[path = { ? <1> }, target = return, annotation = @java.lang.Override[visible = true]()]") shouldEqual {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), true)
      new MethodTypeAnnotation(typePath, annotation, new ReturnType())
    }
  }
}
