package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.Classfile.{EnclosingMethod, Version}
import me.aki.tactical.core.annotation.Annotation
import me.aki.tactical.core.typeannotation.ClassTypeAnnotation

class ClassfileParser(bodyParser: BodyParser) extends Parser[Classfile] {
  val parser: P[Classfile] = P {
    val pkg = "package" ~ WS ~ Literal.rep(min = 1, sep = WS.? ~ "." ~ WS.?) ~ WS.? ~ ";"
    val version = "version" ~ WS ~ int ~ WS.? ~ "." ~ WS.? ~ int ~ WS.? ~ ";"

    val classDescriptor = P {
      val classKeywordParser = Seq(
        for (_ ← "class".!) yield None,
        for (_ ← "interface".!) yield Some(Classfile.Flag.INTERFACE),
        for (_ ← "@interface".!) yield Some(Classfile.Flag.ANNOTATION),
        for (_ ← "enum".!) yield Some(Classfile.Flag.ENUM),
        for (_ ← "module".!) yield Some(Classfile.Flag.MODULE)
      ).reduce(_ | _)

      val superclass = P { "extends" ~ WS ~ PathParser }
      val interfaces = P { "implements" ~ WS ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) }

      ClassFlagParser ~ classKeywordParser ~ WS ~ Literal ~ WS ~ superclass.? ~ WS.? ~ interfaces.?
    }

    P {
      pkg ~ WS.? ~ version ~ WS.? ~ ClassPrefixParser.rep(sep = WS.?) ~ WS.? ~
        classDescriptor ~ WS.? ~ "{" ~ WS.?
    } flatMap {
      case (pkg, (major, minor), prefixes, (flags, classTypeFlag, name, superType, interfaces)) =>
        val classfile = {
          val version = new Version(major.toInt, minor.toInt)
          val fullName = new Path(pkg.asJava, name)
          new Classfile(version, fullName, superType.orNull, interfaces.getOrElse(Nil).asJava)
        }

        classfile.getFlags.addAll(flags)
        classTypeFlag.foreach(classfile.getFlags.add)

        prefixes.foreach(_ apply classfile)

        for (contents ← new ClassContentParser(classfile, bodyParser).rep(sep = WS.?) ~ WS.? ~ "}") yield {
          contents.foreach(_ apply classfile)
          classfile
        }
    }
  }
}

sealed trait ClassPrefix extends (Classfile => Unit)
object ClassPrefix {
  class SignaturePrefix(signature: String) extends ClassPrefix {
    def apply(classfile: Classfile): Unit = classfile.setSignature(Optional.of(signature))
  }

  class SourcePrefix(source: String) extends ClassPrefix {
    def apply(classfile: Classfile): Unit = classfile.setSource(Optional.of(source))
  }

  class DebugPrefix(source: String) extends ClassPrefix {
    def apply(classfile: Classfile): Unit = classfile.setSourceDebug(Optional.of(source))
  }

  class AnnotationPrefix(anno: Annotation) extends ClassPrefix {
    def apply(classfile: Classfile): Unit = classfile.getAnnotations().add(anno)
  }

  class TypeAnnotationPrefix(typeAnnotation: ClassTypeAnnotation) extends ClassPrefix {
    def apply(classfile : Classfile): Unit = classfile.getTypeAnnotations().add(typeAnnotation)
  }

  class AttributePrefix(attribute: Attribute) extends ClassPrefix {
    def apply(classfile: Classfile): Unit = classfile.getAttributes().add(attribute)
  }
}

object ClassPrefixParser extends Parser[ClassPrefix] {
  object SignaturePrefixParser extends Parser[ClassPrefix.SignaturePrefix] {
    val parser: P[ClassPrefix.SignaturePrefix] = P {
      for (signature ← "signature" ~ WS ~ StringLiteral ~ WS.? ~ ";")
        yield new ClassPrefix.SignaturePrefix(signature)
    } opaque "<signature>"
  }

  object SourcePrefixParser extends Parser[ClassPrefix.SourcePrefix] {
    val parser: P[ClassPrefix.SourcePrefix] = P {
      for (source ← "source" ~ WS ~ StringLiteral ~ WS.? ~ ";")
        yield new ClassPrefix.SourcePrefix(source)
    } opaque "<source-file>"
  }

  object DebugPrefixParser extends Parser[ClassPrefix.DebugPrefix] {
    val parser: P[ClassPrefix.DebugPrefix] = P {
      for (debug ← "debug" ~ WS ~ StringLiteral ~ WS.? ~ ";")
        yield new ClassPrefix.DebugPrefix(debug)
    } opaque "<debug-source>"
  }

  object AnnotationPrefixParser extends Parser[ClassPrefix.AnnotationPrefix] {
    val parser: P[ClassPrefix.AnnotationPrefix] = P {
      for (annotation ← AnnotationParser ~ WS.? ~ ";".?) yield new ClassPrefix.AnnotationPrefix(annotation)
    } opaque "<annotation>"
  }

  object TypeAnnotationPrefixParser extends Parser[ClassPrefix.TypeAnnotationPrefix] {
    val parser: P[ClassPrefix.TypeAnnotationPrefix] = P {
      for (annotation ← ClassTypeAnnotationParser ~ WS.? ~ ";") yield new ClassPrefix.TypeAnnotationPrefix(annotation)
    } opaque "<type-annotation>"
  }

  object AttributePrefixParser extends Parser[ClassPrefix.AttributePrefix] {
    val parser: P[ClassPrefix.AttributePrefix] = P {
      for(attribute ← AttributeParser ~ WS.? ~ ";".?) yield new ClassPrefix.AttributePrefix(attribute)
    } opaque "<attribute>"
  }

  val parser: P[ClassPrefix] = P {
    SignaturePrefixParser |
      SourcePrefixParser |
      DebugPrefixParser |
      AnnotationPrefixParser |
      TypeAnnotationPrefixParser |
      AttributePrefixParser
  }
}

trait ClassContent extends (Classfile => Unit)
object ClassContent {
  class ModuleContent(module: Module) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.setModule(Optional.of(module))
  }

  class EnclosingContent(enclosing: Classfile.EnclosingMethod) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.setEnclosingMethod(Optional.of(enclosing))
  }

  class InnerClassContent(innerClass: Classfile.InnerClass) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.getInnerClasses.add(innerClass)
  }

  class NestHostContent(host: Path) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.setNestHost(Optional.of(host))
  }

  class NestMemberContent(members: Seq[Path]) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.getNestMembers.addAll(members.asJava)
  }

  class FieldContent(field: Field) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.getFields.add(field)
  }

  class MethodContent(method: Method) extends ClassContent {
    override def apply(clazz: Classfile): Unit = clazz.getMethods.add(method)
  }
}

object ClassContentParser {
  object ModuleContentParser extends Parser[ClassContent.ModuleContent] {
    val parser: P[ClassContent.ModuleContent] = P {
      for (module ← ModuleParser) yield new ClassContent.ModuleContent(module)
    } opaque "<module>"
  }

  object EnclosingContentParser extends Parser[ClassContent.EnclosingContent] {
    val parser: P[ClassContent.EnclosingContent] = P {
      val name = "name" ~ WS.? ~ "=" ~ WS.? ~ StringLiteral ~ WS.? ~ ";"
      val descriptor = "descriptor" ~ WS.? ~ "=" ~ WS.? ~ MethodDescriptorParser ~ WS.? ~ ";"

      for ((owner, name, descriptor) ← "enclosing" ~ WS.? ~ PathParser ~ WS.? ~ "{" ~ WS.? ~ (name ~ WS.?).? ~ (descriptor ~ WS.?).? ~ "}")
        yield new ClassContent.EnclosingContent(new EnclosingMethod(owner, Optional.ofNullable(name.orNull), Optional.ofNullable(descriptor.orNull)))
    } opaque "<enclosing-method>"
  }

  object InnerClassContentParser extends Parser[ClassContent.InnerClassContent] {
    val parser: P[ClassContent.InnerClassContent] = P {
      sealed trait InnerContent extends (Classfile.InnerClass => Unit)
      class Inner(inner: String) extends InnerContent {
        override def apply(cinner: Classfile.InnerClass): Unit =
          cinner.setInnerName(Optional.of(inner))
      }
      class Outer(outer: Path) extends InnerContent {
        override def apply(cinner: Classfile.InnerClass): Unit =
          cinner.setOuterName(Optional.of(outer))
      }

      val inner = for (inner ← "inner" ~ WS ~ StringLiteral ~ ";") yield new Inner(inner)
      val outer = for (outer ← "outer" ~ WS ~ PathParser ~ ";") yield new Outer(outer)

      for ((flags, name, content) ← InnerClassFlagParser ~ "inner" ~ WS ~ PathParser ~ WS.? ~ "{" ~ WS.? ~
        (inner | outer).rep(sep = WS.?) ~ WS.? ~ "}") yield {
        val inner = new Classfile.InnerClass(name)
        inner.setFlags(flags)
        content.foreach(_ apply inner)
        new ClassContent.InnerClassContent(inner)
      }
    } opaque "<inner-class>"
  }

  object NestHostContentParser extends Parser[ClassContent.NestHostContent] {
    val parser: P[ClassContent.NestHostContent] = P {
      for (host ← "nest" ~ WS ~ "host" ~ WS ~ PathParser ~ WS.? ~ ";")
        yield new ClassContent.NestHostContent(host)
    } opaque "<nest-host>"
  }

  object NestMemberContentParser extends Parser[ClassContent.NestMemberContent] {
    val parser: P[ClassContent.NestMemberContent] = P {
      for (hosts ← "nest" ~ WS ~ "member" ~ WS ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ ";")
        yield new ClassContent.NestMemberContent(hosts)
    } opaque "<nest-member>"
  }

  object FieldContentParser extends Parser[ClassContent.FieldContent] {
    val parser: P[ClassContent.FieldContent] = P {
      for (field ← FieldParser) yield new ClassContent.FieldContent(field)
    } opaque "<field>"
  }

  class MethodContentParser(classfile: Classfile, bodyParser: BodyParser) extends Parser[ClassContent.MethodContent] {
    val parser: P[ClassContent.MethodContent] = P {
      for (method ← new MethodParser(classfile, bodyParser)) yield new ClassContent.MethodContent(method)
    } opaque "<method>"
  }
}

class ClassContentParser(classfile: Classfile, bodyParser: BodyParser) extends Parser[ClassContent] {
  import ClassContentParser._

  val parser: P[ClassContent] = P {
    ClassContentParser.ModuleContentParser |
      EnclosingContentParser |
      InnerClassContentParser |
      NestHostContentParser |
      NestMemberContentParser |
      FieldContentParser |
      new MethodContentParser(classfile, bodyParser)
  }
}
