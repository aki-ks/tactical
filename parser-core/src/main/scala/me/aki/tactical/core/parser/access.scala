package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import java.util.{HashSet => JHashSet, Set => JSet}

import fastparse.all._
import me.aki.tactical.core.{ Classfile, Module }

class AccessParser[F <: Enum[F]](flags: (String, F)*) extends Parser[JSet[F]] {
  val parser: P[JSet[F]] = {
    val flagParser = {
      for ((name, flag) ← flags)
        yield P[F] { (name ~ WS).!.map(_ => flag) }
    } reduce (_ | _)

    flagParser.rep(sep = WS).map(x => new JHashSet(x.asJava))
  }
}

object ClassAccessParser extends AccessParser[Classfile.Flag](
  "public" -> Classfile.Flag.PUBLIC,
  "final" -> Classfile.Flag.FINAL,
  "super" -> Classfile.Flag.SUPER,
  "abstract" -> Classfile.Flag.ABSTRACT,
  "synthetic" -> Classfile.Flag.SYNTHETIC
)

object ModuleAccessParser extends AccessParser[Module.Flag](
  "open" -> Module.Flag.OPEN,
  "synthetic" -> Module.Flag.SYNTHETIC,
  "mandated" -> Module.Flag.MANDATED
)

object ModuleRequireAccessParser extends AccessParser[Module.Require.Flag](
  "transitive" -> Module.Require.Flag.TRANSITIVE,
  "static-phase" -> Module.Require.Flag.STATIC_PHASE,
  "synthetic" -> Module.Require.Flag.SYNTHETIC,
  "mandated" -> Module.Require.Flag.MANDATED
)

object ModuleExportAccessParser extends AccessParser[Module.Export.Flag](
  "synthetic" -> Module.Export.Flag.SYNTHETIC,
  "mandated" -> Module.Export.Flag.MANDATED
)

object ModuleOpensAccessParser extends AccessParser[Module.Open.Flag](
  "synthetic" -> Module.Open.Flag.SYNTHETIC,
  "mandated" -> Module.Open.Flag.MANDATED
)
