package me.aki.tactical.stack.parser

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation
import me.aki.tactical.stack.StackBody.{LineNumber, LocalVariable, LocalVariableAnnotation}
import me.aki.tactical.stack.TryCatchBlock
import me.aki.tactical.stack.insn.Instruction

class TryCatchBlockParser(ctx: ResolvedStackCtx) extends Parser[TryCatchBlock] {
  val parser: P[TryCatchBlock] = P {
    val label: P[Instruction] = Literal.map(ctx.getLabel) opaque "label"

    for ((first, last, handler, exception) ← "try" ~ WS.? ~ label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ "catch" ~ WS.? ~ label ~ WS.? ~ (":" ~ WS.? ~ PathParser ~ WS.?).? ~ ";")
      yield new TryCatchBlock(first, last, handler, Optional.ofNullable(exception.orNull))
  }
}

class LineNumberParser(ctx: ResolvedStackCtx) extends Parser[LineNumber] {
  val parser: P[LineNumber] = P {
    val label: P[Instruction] = Literal.map(ctx.getLabel) opaque "label"

    for ((line, label) ← "line" ~ WS.? ~ int ~ WS.? ~ label ~ WS.? ~ ";")
      yield new LineNumber(line, label)
  }
}

class LocalVariableParser(ctx: ResolvedStackCtx) extends Parser[LocalVariable] {
  val parser: P[LocalVariable] = P {
    val label: P[Instruction] = Literal.map(ctx.getLabel) opaque "label"

    for {
      (start, end, local, name, typ, signature) ←
        "local" ~ WS.? ~ "info" ~ WS.? ~
          label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ "," ~ WS.? ~ // range
          Literal ~ WS.? ~ "," ~ WS.? ~ // local
          StringLiteral ~ WS.? ~ "," ~ WS.? ~ // name
          TypeParser ~ WS.? ~ // type
          ("," ~ WS.? ~ StringLiteral ~ WS.?).? ~ // signature
          ";"
    } yield new LocalVariable(name, typ, Optional.ofNullable(signature.orNull), start, end, ctx.getLocal(local))
  }
}

class LocalVariableAnnotationParser(ctx: ResolvedStackCtx) extends Parser[LocalVariableAnnotation] {
  val parser: P[LocalVariableAnnotation] = P {
    val label: P[Instruction] = Literal.map(ctx.getLabel) opaque "label"

    val locations = {
      val locationParser = P {
        for ((start, end, local) ← label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ Literal)
          yield new LocalVariableAnnotation.Location(start, end, ctx.getLocal(local))
      }

      "[" ~ WS.? ~ locationParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "]"
    }

    for ((locations, annotation) ← "local" ~ WS.? ~ "annotation" ~ WS.? ~ locations ~ WS.? ~ LocalTypeAnnotationParser ~ WS.? ~ ";")
      yield new LocalVariableAnnotation(annotation, locations.asJava)
  }
}
