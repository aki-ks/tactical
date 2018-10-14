package me.aki.tactical.core.parser

import java.util.{ArrayList, Optional, List => JList}

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.`type`.Type
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation

class MethodParser(classfile: Classfile, bodyParser: Parser[Body]) extends Parser[Method] {
  val parser: P[Method] = P {
    P {
      val throws = "throws" ~ WS ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?)
      val params = TypeParser.rep(sep = WS.? ~ "," ~ WS.?)


      val descriptor = P {
        val void = Optional.empty[Type]

        val staticInitializerForm =
          for ((flags, exceptions) ← StaticInitializerFlagParser ~ "static" ~ WS ~ throws.? ~ WS.?)
            yield (flags, void, "<clinit>", Nil, exceptions)

        val constructorForm =
          for((flags, params, exceptions) ← MethodFlagParser ~ classfile.getName.getName ~ "(" ~ params ~ ")" ~ WS.? ~ throws.? ~ WS.?)
            yield (flags, void, "<init>", params, exceptions)

        val methodForm =
          MethodFlagParser ~ ReturnTypeParser ~ WS ~ Literal ~ WS.? ~ "(" ~ params ~ ")" ~ WS.? ~ throws.? ~ WS.?

        constructorForm | methodForm | staticInitializerForm
      }

      MethodPrefixParser.rep(sep = WS.?) ~ WS.? ~ descriptor
    } flatMap {
      case (prefixes, (flags, returnType, name, parameters, exceptions)) =>
        val method = new Method(name, new ArrayList(parameters.asJava), returnType)
        method.setFlags(flags)
        exceptions.getOrElse(Nil).foreach(method.getExceptions.add)
        prefixes.foreach(_ apply method)

        val abstractForm =
          for (_  ← P(";")) yield method

        val annotationForm =
          for (value ← "=" ~ WS.? ~ AnnotationValueParser ~ WS.? ~ ";") yield {
            method.setDefaultValue(Optional.of(value))
            method
          }

        val bodyForm =
          for (body ← "{" ~ WS.? ~ bodyParser ~ WS.? ~ "}") yield {
            method.setBody(Optional.of(body))
            method
          }

        abstractForm | annotationForm | bodyForm
    }
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
      for (signature ← "signature" ~ WS ~ StringLiteral ~ WS.? ~ ";")
        yield new MethodPrefix.SignaturePrefix(signature)
    } opaque "<signature>"
  }

  object ParameterInfoPrefixParser extends Parser[MethodPrefix.ParameterInfoPrefix] {
    val parser: P[MethodPrefix.ParameterInfoPrefix] = P {
      for ((flags, name) ← ParameterFlagParser ~ "parameter" ~ WS.? ~ (StringLiteral ~ WS.?).? ~ ";")
        yield new MethodPrefix.ParameterInfoPrefix(new Method.Parameter(Optional.ofNullable(name.orNull), flags))
    } opaque "<parameter-info>"
  }

  object ParameterAnnotationsPrefixParser extends Parser[MethodPrefix.ParameterAnnotationPrefix] {
    val parser: P[MethodPrefix.ParameterAnnotationPrefix] = P {
      for (annotations ← "parameter" ~ WS ~ "annotations" ~ WS.? ~ "{" ~ WS.? ~ AnnotationParser.rep(sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "}")
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
      for (annotation ← MethodTypeAnnotationParser ~ WS.? ~ ";") yield new MethodPrefix.TypeAnnotationPrefix(annotation)
    } opaque "<type-annotation>"
  }

  object AttributePrefixParser extends Parser[MethodPrefix.AttributePrefix] {
    val parser: P[MethodPrefix.AttributePrefix] = P {
      for(attribute ← AttributeParser ~ WS.? ~ ";".?) yield new MethodPrefix.AttributePrefix(attribute)
    } opaque "<attribute>"
  }

  val parser: P[MethodPrefix] = P {
    SignaturePrefixParser |
      ParameterInfoPrefixParser |
      ParameterAnnotationsPrefixParser |
      AnnotationPrefixParser |
      TypeAnnotationPrefixParser |
      AttributePrefixParser
  }
}