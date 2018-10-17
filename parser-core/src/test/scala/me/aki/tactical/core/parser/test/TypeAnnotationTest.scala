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

  "The ClassTargetTypeParser" should "parse all kinds of ClassTargetType" in {
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

  "The LocalTargetTypeParser" should "parse all kinds of LocalTargetTypes" in {
    LocalTargetTypeParser.parse("local") shouldEqual new LocalVariable
    LocalTargetTypeParser.parse("resource") shouldEqual new ResourceVariable
  }

  "The InsnTargetTypeParser" should "parse all kinds of InsnTargetTypes" in {
    InsnTargetTypeParser.parse("new") shouldEqual new New

    forAll { intersection: Int =>
      InsnTargetTypeParser.parse(s"cast $intersection") shouldEqual new Cast(intersection)
    }

    InsnTargetTypeParser.parse("instanceof") shouldEqual new InstanceOf

    InsnTargetTypeParser.parse("constructor reference") shouldEqual new ConstructorReference

    forAll { parameter: Int =>
      InsnTargetTypeParser.parse(s"constructor invoke type parameter $parameter") shouldEqual new ConstructorInvokeTypeParameter(parameter)
    }

    forAll { parameter: Int =>
      InsnTargetTypeParser.parse(s"constructor reference type parameter $parameter") shouldEqual new ConstructorReferenceTypeParameter(parameter)
    }

    InsnTargetTypeParser.parse("method reference") shouldEqual new MethodReference

    forAll { parameter: Int =>
      InsnTargetTypeParser.parse(s"method invoke type parameter $parameter") shouldEqual new MethodInvokeTypeParameter(parameter)
    }

    forAll { parameter: Int =>
      InsnTargetTypeParser.parse(s"method reference type parameter $parameter") shouldEqual new MethodReferenceTypeParameter(parameter)
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

  "The FieldTypeAnnotationParser" should "parse field type annotations" in {
    FieldTypeAnnotationParser.parse("#[path = { ? <1> }, annotation = @java.lang.Deprecated[visible = true]()]") shouldEqual {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), true)
      new FieldTypeAnnotation(typePath, annotation)
    }
  }

  "The InsnTypeAnnotationParser" should "parse insn type annotations" in {
    InsnTypeAnnotationParser.parse("#[path = { ? <1> }, target = constructor reference type parameter 9, annotation = @java.lang.Override[visible = false]()]") shouldEqual {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), false)
      new InsnTypeAnnotation(typePath, annotation, new ConstructorReferenceTypeParameter(9))
    }
  }

  "The LocalTypeAnnotationParser" should "parse local type annotations" in {
    LocalTypeAnnotationParser.parse("#[path = { ? <1> }, target = local, annotation = @java.lang.Override[visible = false]()]") shouldEqual {
      val typePath = new TypePath(List(new Kind.WildcardBound(), new Kind.TypeArgument(1)).asJava)
      val annotation = new Annotation(Path.of("java", "lang", "Object"), false)
      new LocalVariableTypeAnnotation(typePath, annotation, new LocalVariable())
    }
  }
}
