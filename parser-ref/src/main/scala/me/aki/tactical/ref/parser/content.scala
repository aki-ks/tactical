package me.aki.tactical.ref.parser

import scala.collection.JavaConverters._
import java.util.Optional
import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref._

class TryCatchBlockParser(ctx: ResolvedRefCtx) extends Parser[TryCatchBlock] {
  val parser: P[TryCatchBlock] = {
    val label = new LabelParser(ctx)
    val local = new LocalParser(ctx)

    for ((start, end, handler, local, exception) ← "try" ~ WS.? ~ label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ "catch" ~ WS.? ~ label ~ WS.? ~ local ~ WS.? ~ (":" ~ WS.? ~ PathParser ~ WS.?).? ~ ";")
      yield new TryCatchBlock(start, end, handler, Optional.ofNullable(exception.orNull), local)
  }
}

class LineParser(ctx: ResolvedRefCtx) extends Parser[RefBody.LineNumber] {
  val parser: P[RefBody.LineNumber] = {
    val label = new LabelParser(ctx)

    for ((line, label) ← "line" ~ WS.? ~ int ~ WS.? ~ label ~ WS.? ~ ";")
      yield new RefBody.LineNumber(line, label)
  }
}

class LocalVariableParser(ctx: ResolvedRefCtx) extends Parser[RefBody.LocalVariable] {
  val parser: P[RefBody.LocalVariable] = {
    val label = new LabelParser(ctx)
    val local = new LocalParser(ctx)

    for ((start, end, local, name, typ, signature) ← "local" ~ WS ~ "info" ~ WS.? ~ label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ local ~ WS.? ~ StringLiteral ~ WS.? ~ TypeParser ~ WS.? ~ (StringLiteral ~ WS.?).? ~ ";")
      yield new RefBody.LocalVariable(name, typ, Optional.ofNullable(signature.orNull), start, end, local)
  }
}

class LocalVariableAnnotationParser(ctx: ResolvedRefCtx) extends Parser[RefBody.LocalVariableAnnotation] {
  val parser: P[RefBody.LocalVariableAnnotation] = {
    val label = new LabelParser(ctx)
    val local = new LocalParser(ctx)

    val location =
      for ((start, end, local) ← label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ local)
        yield new RefBody.LocalVariableAnnotation.Location(start, end, local)

    for ((locations, annotation) ← "local" ~ WS ~ "annotation" ~ WS.? ~ "[" ~ WS.? ~ location.rep(sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "]" ~ WS.? ~ LocalTypeAnnotationParser)
      yield new RefBody.LocalVariableAnnotation(annotation, locations.asJava)
  }
}
