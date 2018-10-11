package me.aki.tactical.core.parser

import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.{Attribute, Field}
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.typeannotation.FieldTypeAnnotation

object FieldParser extends Parser[Field] {
  val parser: P[Field] =
    P {
      FieldPrefixParser.rep(sep = WS.?) ~ WS.? ~ FieldFlagParser ~ WS.? ~ TypeParser ~ WS ~ Literal ~ WS.? ~ ("=" ~ WS.? ~ FieldConstantParser ~ WS.?).?  ~ ";"
    } map {
      case (prefixes, flags, typ, name, constant) =>
        val field = new Field(name, typ)
        field.getFlags.addAll(flags)
        field.setValue(Optional.ofNullable(constant.orNull))
        prefixes foreach (_ apply field)
        field
    }
}

trait FieldPrefix extends (Field => Unit)
object FieldPrefix {
  class SignaturePrefix(signature: String) extends FieldPrefix {
    def apply(field: Field) = field.setSignature(Optional.of(signature))
  }

  class AnnotationPrefix(annotation: Annotation) extends FieldPrefix {
    def apply(field: Field) = field.getAnnotations.add(annotation)
  }

  class TypeAnnotationPrefix(annotation: FieldTypeAnnotation) extends FieldPrefix {
    def apply(field: Field) = field.getTypeAnnotations.add(annotation)
  }

  class AttributePrefix(attribute: Attribute) extends FieldPrefix {
    def apply(field: Field) = field.getAttributes.add(attribute)
  }
}

object FieldPrefixParser extends Parser[FieldPrefix] {
  object SignaturePrefixParser extends Parser[FieldPrefix.SignaturePrefix] {
    val parser: P[FieldPrefix.SignaturePrefix] = P {
      for (signature ← "signature" ~ WS ~ StringLiteral ~ WS.? ~ ";")
        yield new FieldPrefix.SignaturePrefix(signature)
    } opaque "<signature>"
  }

  object AnnotationPrefixParser extends Parser[FieldPrefix.AnnotationPrefix] {
    val parser: P[FieldPrefix.AnnotationPrefix] = P {
      for (annotation ← AnnotationParser ~ WS.? ~ ";".?) yield new FieldPrefix.AnnotationPrefix(annotation)
    } opaque "<annotation>"
  }

  object TypeAnnotationPrefixParser extends Parser[FieldPrefix.TypeAnnotationPrefix] {
    val parser: P[FieldPrefix.TypeAnnotationPrefix] = P {
      for (annotation ← FieldTypeAnnotationParser ~ WS.? ~ ";") yield new FieldPrefix.TypeAnnotationPrefix(annotation)
    } opaque "<type-annotation>"
  }

  object AttributePrefixParser extends Parser[FieldPrefix.AttributePrefix] {
    val parser: P[FieldPrefix.AttributePrefix] = P {
      for(attribute ← AttributeParser ~ WS.? ~ ";".?) yield new FieldPrefix.AttributePrefix(attribute)
    } opaque "<attribute>"
  }

  val parser: P[FieldPrefix] = P {
    SignaturePrefixParser |
      AnnotationPrefixParser |
      TypeAnnotationPrefixParser |
      AttributePrefixParser
  }
}
