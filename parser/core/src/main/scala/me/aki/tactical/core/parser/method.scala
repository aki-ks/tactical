package me.aki.tactical.core.parser

import java.util.{ArrayList, Optional, List => JList, Set => JSet}
import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.`type`.Type
import me.aki.tactical.core.annotation.{Annotation, AnnotationValue}
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation

trait BodyParser {
  /**
    * Additional data collected by the parameter list parser
    */
  type Ctx

  /** Get a content for the static initializer */
  def staticInitializerCtx: Ctx

  /** A parser for the parameter list of a method */
  def parameterParser: P[(Ctx, List[Type])]

  def bodyParser(classfile: Classfile, method: Method, ctx: Ctx): P[Body]
}

class MethodParser[B <: BodyParser](classfile: Classfile, bodyParser: B) extends Parser[Method] {
  private type MethodDescriptor = (JSet[Method.Flag], Optional[Type], String, (bodyParser.type#Ctx, List[Type]), Option[Seq[Path]])

  def methodParser(descriptorParser: P[MethodDescriptor], contentParser: (Method, bodyParser.type, bodyParser.type#Ctx) => P[MethodContent]) = P {
    MethodPrefixParser.rep(sep = WS.?) ~ WS.? ~ descriptorParser ~ WS.?
  } flatMap {
    case (prefixes, (flags, returnType, name, (paramCtx, paramTypes), exceptions)) =>
      val method = new Method(name, new ArrayList(paramTypes.asJava), returnType)
      method.setFlags(flags)
      exceptions.getOrElse(Nil).foreach(method.getExceptions.add)
      prefixes.foreach(_ apply method)

      for (methodContent ← contentParser(method, bodyParser, paramCtx)) yield {
        methodContent.apply(method)
        method
      }
  }

  val parser: P[Method] = {
    val throws = "throws" ~ WS ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?)
    val void = Optional.empty[Type]

    val staticInitializerForm =
      for ((flags, exceptions) ← StaticInitializerFlagParser ~ "static" ~ WS ~ throws.? ~ WS.?)
        yield (flags, void, "<clinit>", (bodyParser.staticInitializerCtx, Nil), exceptions)

    val constructorForm =
      for ((flags, params, exceptions) ← MethodFlagParser ~ classfile.getName.getName ~ "(" ~ bodyParser.parameterParser ~ ")" ~ WS.? ~ throws.? ~ WS.?)
        yield (flags, void, "<init>", params, exceptions)

    val methodForm =
      for ((flags, returnType, name, params, exceptions) ← MethodFlagParser ~ ReturnTypeParser ~ WS ~ Literal ~ WS.? ~ "(" ~ bodyParser.parameterParser ~ ")" ~ WS.? ~ throws.? ~ WS.?)
        yield (flags, returnType, name, params, exceptions)

    methodParser(staticInitializerForm, (method, bp, ctx) => new MethodContentParser.BodyFormParser[bp.type#Ctx, bp.type](classfile, method, bp, ctx)) |
      methodParser(constructorForm, (method, bp, ctx) => new MethodContentParser.BodyFormParser[bp.type#Ctx, bp.type](classfile, method, bp, ctx)) |
      methodParser(methodForm, (method, bp, ctx) => MethodContentParser.AbstractFormParser | MethodContentParser.AnnotationFormParser | new MethodContentParser.BodyFormParser[bp.type#Ctx, bp.type](classfile, method, bp, ctx))
  }
}

trait MethodContent extends (Method => Unit)
object MethodContent {
  object AbstractForm extends MethodContent {
    def apply(method: Method) {}
  }

  class AnnotationForm(value: AnnotationValue) extends MethodContent {
    def apply(method: Method) = method.setDefaultValue(Optional.of(value))
  }

  class BodyForm(body: Body) extends MethodContent {
    def apply(method: Method) = method.setBody(Optional.of(body))
  }
}

object MethodContentParser {
  object AbstractFormParser extends Parser[MethodContent.AbstractForm.type] {
    val parser: P[MethodContent.AbstractForm.type] =
      for (_  ← P(";")) yield MethodContent.AbstractForm
  }

  object AnnotationFormParser extends Parser[MethodContent.AnnotationForm] {
    val parser: P[MethodContent.AnnotationForm] =
      for (value ← "=" ~ WS.? ~ AnnotationValueParser ~ WS.? ~ ";")
        yield new MethodContent.AnnotationForm(value)
  }

  class BodyFormParser[C, B <: BodyParser { type Ctx = C }](classfile: Classfile, method: Method, bp: B, ctx: C) extends Parser[MethodContent.BodyForm] {
    val parser: P[MethodContent.BodyForm] =
      for (body ← "{" ~ WS.? ~ bp.bodyParser(classfile, method, ctx) ~ WS.? ~ "}")
        yield new MethodContent.BodyForm(body)
  }
}

trait MethodPrefix extends (Method => Unit)
object MethodPrefix {
  class SignaturePrefix(signature: String) extends MethodPrefix {
    def apply(method: Method): Unit = method.setSignature(Optional.of(signature))
  }

  class ParameterInfoPrefix(parameter: Method.Parameter) extends MethodPrefix {
    def apply(method: Method): Unit = method.getParameterInfo.add(parameter)
  }

  class ParameterAnnotationPrefix(paramAnnos: JList[Annotation]) extends MethodPrefix {
    def apply(method: Method): Unit = method.getParameterAnnotations.add(paramAnnos)
  }

  class AnnotationPrefix(annotation: Annotation) extends MethodPrefix {
    def apply(method: Method): Unit = method.getAnnotations.add(annotation)
  }

  class TypeAnnotationPrefix(annotation: MethodTypeAnnotation) extends MethodPrefix {
    def apply(method: Method): Unit = method.getTypeAnnotations.add(annotation)
  }

  class AttributePrefix(attribute: Attribute) extends MethodPrefix {
    def apply(method: Method): Unit = method.getAttributes.add(attribute)
  }
}

object MethodPrefixParser extends Parser[MethodPrefix] {
  object SignaturePrefixParser extends Parser[MethodPrefix.SignaturePrefix] {
    val parser: P[MethodPrefix.SignaturePrefix] = P {
      for (signature ← "signature" ~ WS ~ StringLiteral ~ WS.? ~ ";".?)
        yield new MethodPrefix.SignaturePrefix(signature)
    } opaque "<signature>"
  }

  object ParameterInfoPrefixParser extends Parser[MethodPrefix.ParameterInfoPrefix] {
    val parser: P[MethodPrefix.ParameterInfoPrefix] = P {
      for ((flags, name) ← ParameterFlagParser ~ "parameter" ~ WS.? ~ (StringLiteral ~ WS.?).? ~ ";".?)
        yield new MethodPrefix.ParameterInfoPrefix(new Method.Parameter(Optional.ofNullable(name.orNull), flags))
    } opaque "<parameter-info>"
  }

  object ParameterAnnotationsPrefixParser extends Parser[MethodPrefix.ParameterAnnotationPrefix] {
    val parser: P[MethodPrefix.ParameterAnnotationPrefix] = P {
      for (annotations ← "parameter" ~ WS ~ "annotations" ~ WS.? ~ "{" ~ WS.? ~ AnnotationParser.rep(sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "}" ~ WS.? ~ ";".?)
        yield new MethodPrefix.ParameterAnnotationPrefix(annotations.asJava)
    } opaque "<parameter-annotations>"
  }

  object AnnotationPrefixParser extends Parser[MethodPrefix.AnnotationPrefix] {
    val parser: P[MethodPrefix.AnnotationPrefix] = P {
      for (annotation ← AnnotationParser ~ WS.? ~ ";".?) yield new MethodPrefix.AnnotationPrefix(annotation)
    } opaque "<annotation>"
  }

  object TypeAnnotationPrefixParser extends Parser[MethodPrefix.TypeAnnotationPrefix] {
    val parser: P[MethodPrefix.TypeAnnotationPrefix] = P {
      for (annotation ← MethodTypeAnnotationParser ~ WS.? ~ ";".?) yield new MethodPrefix.TypeAnnotationPrefix(annotation)
    } opaque "<type-annotation>"
  }

  object AttributePrefixParser extends Parser[MethodPrefix.AttributePrefix] {
    val parser: P[MethodPrefix.AttributePrefix] = P {
      for(attribute ← AttributeParser ~ WS.? ~ ";".?) yield new MethodPrefix.AttributePrefix(attribute)
    } opaque "<attribute>"
  }

  val parser: P[MethodPrefix] = P {
    SignaturePrefixParser |
      ParameterAnnotationsPrefixParser |
      ParameterInfoPrefixParser |
      AnnotationPrefixParser |
      TypeAnnotationPrefixParser |
      AttributePrefixParser
  }
}
