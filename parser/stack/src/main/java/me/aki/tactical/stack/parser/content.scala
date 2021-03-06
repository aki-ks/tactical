package me.aki.tactical.stack.parser

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation
import me.aki.tactical.stack.StackBody.{LineNumber, LocalVariable, LocalVariableAnnotation}
import me.aki.tactical.stack.TryCatchBlock
import me.aki.tactical.stack.insn.Instruction

class Label(ctx: ResolvedStackCtx) extends Parser[Instruction] {
  val parser: P[Instruction] = P {
    Literal.map(ctx.getLabelOpt)
      .filter(_.isDefined).map(_.get)
  } opaque "label"
}

class TryCatchBlockParser(ctx: ResolvedStackCtx) extends Parser[TryCatchBlock] {
  val parser: P[TryCatchBlock] = P {
    val label = new Label(ctx)
    for ((first, last, handler, exception) ← "try" ~ WS.? ~ label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ "catch" ~ WS.? ~ label ~ WS.? ~ (":" ~ WS.? ~ PathParser ~ WS.?).? ~ ";")
      yield new TryCatchBlock(first, last, handler, Optional.ofNullable(exception.orNull))
  }
}

class LineNumberParser(ctx: ResolvedStackCtx) extends Parser[LineNumber] {
  val parser: P[LineNumber] = P {
    val label = new Label(ctx)

    for ((line, label) ← "line" ~ WS.? ~ int ~ WS.? ~ label ~ WS.? ~ ";")
      yield new LineNumber(line, label)
  }
}

class LocalVariableParser(ctx: ResolvedStackCtx) extends Parser[LocalVariable] {
  val parser: P[LocalVariable] = P {
    val label = new Label(ctx)

    for {
      (start, end, local, name, typ, signature) ←
        "local" ~ WS.? ~ "info" ~ WS.? ~ label ~ WS.? ~ ("->" | "→") ~ WS.? ~ label ~ WS.? ~ Literal ~ WS.? ~ StringLiteral ~ WS.? ~ TypeParser ~ WS.? ~ (StringLiteral ~ WS.?).? ~ ";"
    } yield new LocalVariable(name, typ, Optional.ofNullable(signature.orNull), start, end, ctx.getLocal(local))
  }
}

class LocalVariableAnnotationParser(ctx: ResolvedStackCtx) extends Parser[LocalVariableAnnotation] {
  val parser: P[LocalVariableAnnotation] = P {
    val label = new Label(ctx)

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

class InsnAnnotationParser(ctx: ResolvedStackCtx) extends Parser[(Instruction, InsnTypeAnnotation)] {
  val parser: P[(Instruction, InsnTypeAnnotation)] = P {
    val label = new Label(ctx)

    "insn" ~ WS.? ~ "annotation" ~ WS.? ~ label ~ WS.? ~ InsnTypeAnnotationParser ~ WS.? ~ ";"
  }
}
