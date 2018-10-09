package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import java.util.{HashSet => JHashSet, Set => JSet}

import fastparse.all._
import me.aki.tactical.core.Classfile

class AccessParser[F <: Enum[F]](flags: (String, F)*) extends Parser[JSet[F]] {
  val parser: P[JSet[F]] = {
    val flagParser = {
      for ((name, flag) ← flags)
        yield P[F] { name.!.map(_ => flag) }
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
