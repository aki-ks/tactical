package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.typeannotation._
import me.aki.tactical.core.typeannotation.TargetType._
import me.aki.tactical.core.typeannotation.TypePath.Kind

object TypePathKindParser extends Parser[Kind] {
  val parser: P[Kind] = P {
    val array = for (_ ← "[]".!) yield new Kind.Array()
    val inner = for (_ ← ".".!) yield new Kind.InnerClass()
    val wildcard = for (_ ← "?".!) yield new Kind.WildcardBound()
    val typeArgument = for (argument ← "<" ~ int ~ ">") yield new Kind.TypeArgument(argument)

    array | inner | wildcard | typeArgument
  }
}

object TypePathParser extends Parser[TypePath] {
  val parser: P[TypePath] = P {
    val kinds = TypePathKindParser.rep(min = 0, sep = WS.?)
    for (kinds ← "{" ~ WS.? ~ kinds ~ WS.? ~ "}") yield new TypePath(kinds.asJava)
  }
}

object TargetTypeParsers {
  import TargetType._
  val checkedException = for (exception ← "exception" ~ WS ~ int) yield new CheckedException(exception)
  val parameters = for (parameter ← "parameter" ~ WS ~ int) yield new MethodParameter(parameter)
  val receiver = for (_ ← "receiver".!) yield new MethodReceiver()
  val returnType = for (_ ← "return".!) yield new ReturnType()
  val typeParameter = for (typeParam ← "type" ~ WS ~ "parameter" ~ WS ~ int) yield new TypeParameter(typeParam)
  val typeParameterBound = for ((param, bound) ← "type" ~ WS ~ "parameter" ~ WS ~ "bound" ~ WS ~ int ~ WS ~ int) yield new TypeParameterBound(param, bound)
  val `extends` = for (_ ← "extends".!) yield new Extends()
  val `implementes` = for (interface ← "implements" ~ WS ~ int) yield new Implements(interface)
  val local = for (_ ← "local".!) yield new LocalVariable
  val resource = for (_ ← "resource".!) yield new ResourceVariable
}

object MethodTargetTypeParser extends Parser[MethodTargetType] {
  val parser: P[MethodTargetType] = P {
    TargetTypeParsers.checkedException |
    TargetTypeParsers.parameters |
    TargetTypeParsers.receiver |
    TargetTypeParsers.returnType |
    TargetTypeParsers.typeParameter |
    TargetTypeParsers.typeParameterBound
  }
}

object ClassTargetTypeParser extends Parser[ClassTargetType] {
  val parser: P[ClassTargetType] = P {
    TargetTypeParsers.`extends` |
    TargetTypeParsers.`implementes` |
    TargetTypeParsers.typeParameter |
    TargetTypeParsers.typeParameterBound
  }
}

object InsnTargetTypeParser extends Parser[InsnTargetType] {
  val parser = ???
}

object LocalTargetTypeParser extends Parser[LocalTargetType] {
  val parser = P {
    TargetTypeParsers.local |
    TargetTypeParsers.resource
  }
}

object ClassTypeAnnotationParser extends Parser[ClassTypeAnnotation] {
  val parser: P[ClassTypeAnnotation] = for {
    (typePath, target, annotation) ← "#" ~ WS.? ~ "[" ~ WS.? ~
      "path" ~ WS.? ~ "=" ~ WS.? ~ TypePathParser ~ WS.? ~ "," ~ WS.? ~
      "target" ~ WS.? ~ "=" ~ WS.? ~ ClassTargetTypeParser ~ WS.? ~ "," ~ WS.? ~
      "annotation" ~ WS.? ~ "=" ~ WS.? ~ AnnotationParser ~ WS.? ~ "]"
  } yield new ClassTypeAnnotation(typePath, annotation, target)
}

object FieldTypeAnnotationParser extends Parser[FieldTypeAnnotation] {
  val parser: P[FieldTypeAnnotation] = for {
    (typePath, annotation) ← "#" ~ WS.? ~ "[" ~ WS.? ~
      "path" ~ WS.? ~ "=" ~ WS.? ~ TypePathParser ~ WS.? ~ "," ~ WS.? ~
      "annotation" ~ WS.? ~ "=" ~ WS.? ~ AnnotationParser ~ WS.? ~ "]"
  } yield new FieldTypeAnnotation(typePath, annotation)
}

object MethodTypeAnnotationParser extends Parser[MethodTypeAnnotation] {
  val parser: P[MethodTypeAnnotation] = for {
    (typePath, target, annotation) ← "#" ~ WS.? ~ "[" ~ WS.? ~
      "path" ~ WS.? ~ "=" ~ WS.? ~ TypePathParser ~ WS.? ~ "," ~ WS.? ~
      "target" ~ WS.? ~ "=" ~ WS.? ~ MethodTargetTypeParser ~ WS.? ~ "," ~ WS.? ~
      "annotation" ~ WS.? ~ "=" ~ WS.? ~ AnnotationParser ~ WS.? ~ "]"
  } yield new MethodTypeAnnotation(typePath, annotation, target)
}

object LocalTypeAnnotationParser extends Parser[LocalVariableTypeAnnotation] {
  val parser: P[LocalVariableTypeAnnotation] = for {
    (typePath, target, annotation) ← "#" ~ WS.? ~ "[" ~ WS.? ~
      "path" ~ WS.? ~ "=" ~ WS.? ~ TypePathParser ~ WS.? ~ "," ~ WS.? ~
      "target" ~ WS.? ~ "=" ~ WS.? ~ LocalTargetTypeParser ~ WS.? ~ "," ~ WS.? ~
      "annotation" ~ WS.? ~ "=" ~ WS.? ~ AnnotationParser ~ WS.? ~ "]"
  } yield new LocalVariableTypeAnnotation(typePath, annotation, target)
}