package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import java.util.{HashSet => JHashSet, Set => JSet}

import fastparse.all._
import me.aki.tactical.core._

class FlagParser[F <: Enum[F]](val flags: (String, F)*) extends Parser[JSet[F]] {
  val parser: P[JSet[F]] = P {
    val flagParser = {
      for ((name, flag) ← flags)
        yield P[F] { (name ~ WS).!.map(_ => flag) }
    } reduce (_ | _)

    for (flags ← flagParser.rep) yield new JHashSet(flags.asJava)
  } opaque "<flags>"
}

object ClassFlagParser extends FlagParser[Classfile.Flag](
  "public" -> Classfile.Flag.PUBLIC,
  "final" -> Classfile.Flag.FINAL,
  "super" -> Classfile.Flag.SUPER,
  "abstract" -> Classfile.Flag.ABSTRACT,
  "synthetic" -> Classfile.Flag.SYNTHETIC
)

object ModuleFlagParser extends FlagParser[Module.Flag](
  "open" -> Module.Flag.OPEN,
  "synthetic" -> Module.Flag.SYNTHETIC,
  "mandated" -> Module.Flag.MANDATED
)

object ModuleRequireFlagParser extends FlagParser[Module.Require.Flag](
  "transitive" -> Module.Require.Flag.TRANSITIVE,
  "static-phase" -> Module.Require.Flag.STATIC_PHASE,
  "synthetic" -> Module.Require.Flag.SYNTHETIC,
  "mandated" -> Module.Require.Flag.MANDATED
)

object ModuleExportFlagParser extends FlagParser[Module.Export.Flag](
  "synthetic" -> Module.Export.Flag.SYNTHETIC,
  "mandated" -> Module.Export.Flag.MANDATED
)

object ModuleOpensFlagParser extends FlagParser[Module.Open.Flag](
  "synthetic" -> Module.Open.Flag.SYNTHETIC,
  "mandated" -> Module.Open.Flag.MANDATED
)

object InnerClassFlagParser extends FlagParser[Classfile.InnerClass.Flag](
  "public" -> Classfile.InnerClass.Flag.PUBLIC,
  "private" -> Classfile.InnerClass.Flag.PRIVATE,
  "protected" -> Classfile.InnerClass.Flag.PROTECTED,
  "static" -> Classfile.InnerClass.Flag.STATIC,
  "final" -> Classfile.InnerClass.Flag.FINAL,
  "abstract" -> Classfile.InnerClass.Flag.ABSTRACT,
  "synthetic" -> Classfile.InnerClass.Flag.SYNTHETIC,

  "interface" -> Classfile.InnerClass.Flag.INTERFACE,
  "@interface" -> Classfile.InnerClass.Flag.ANNOTATION,
  "enum" -> Classfile.InnerClass.Flag.ENUM
)

object FieldFlagParser extends FlagParser[Field.Flag](
  "public" -> Field.Flag.PUBLIC,
  "private" -> Field.Flag.PRIVATE,
  "protected" -> Field.Flag.PROTECTED,
  "static" -> Field.Flag.STATIC,
  "final" -> Field.Flag.FINAL,
  "volatile" -> Field.Flag.VOLATILE,
  "transient" -> Field.Flag.TRANSIENT,
  "synthetic" -> Field.Flag.SYNTHETIC,
  "enum" -> Field.Flag.ENUM
)

/** parse all method flags except 'static' */
object StaticInitializerFlagParser extends FlagParser[Method.Flag] (
  "public" -> Method.Flag.PUBLIC,
  "private" -> Method.Flag.PRIVATE,
  "protected" -> Method.Flag.PROTECTED,
  "final" -> Method.Flag.FINAL,
  "synchronized" -> Method.Flag.SYNCHRONIZED,
  "bridge" -> Method.Flag.BRIDGE,
  "varargs" -> Method.Flag.VARARGS,
  "native" -> Method.Flag.NATIVE,
  "abstract" -> Method.Flag.ABSTRACT,
  "strict" -> Method.Flag.STRICT,
  "synthetic" -> Method.Flag.SYNTHETIC
)

object MethodFlagParser extends FlagParser[Method.Flag](
  ("static" -> Method.Flag.STATIC) +: StaticInitializerFlagParser.flags : _*
)

object ParameterFlagParser extends FlagParser[Method.Parameter.Flag](
  "final" -> Method.Parameter.Flag.FINAL,
  "synthetic" -> Method.Parameter.Flag.SYNTHETIC,
  "mandated" -> Method.Parameter.Flag.MANDATED
)
