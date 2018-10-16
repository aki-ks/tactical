package me.aki.tactical.stack.parser

import scala.collection.JavaConverters._
import java.util.Optional
import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.stack.StackBody.{LineNumber, LocalVariable, LocalVariableAnnotation}
import me.aki.tactical.stack.TryCatchBlock

class TryCatchBlockParser(ctx: StackCtx) extends Parser[TryCatchBlock] {
  val parser: P[TryCatchBlock] = P {
    for {
      (first, last, handler, exception) ←
        "try" ~ WS.? ~ Literal ~ WS.? ~ ("->" | "→") ~ WS.? ~ Literal ~ WS.? ~ "catch" ~ WS.? ~ Literal ~ WS.? ~ (":" ~ WS.? ~ PathParser ~ WS.?).? ~ ";"
    } yield {
      val block = new TryCatchBlock(null, null, null, Optional.ofNullable(exception.orNull))
      ctx.registerLabelReference(first, block.getFirstCell)
      ctx.registerLabelReference(last, block.getLastCell)
      ctx.registerLabelReference(handler, block.getHandlerCell)
      block
    }
  }
}

class LineNumberParser(ctx: StackCtx) extends Parser[LineNumber] {
  val parser: P[LineNumber] = P {
    for ((line, label) ← "line" ~ WS.? ~ int ~ WS.? ~ Literal ~ WS.? ~ ";") yield {
      val node = new LineNumber(line, null)
      ctx.registerLabelReference(label, node.getInstructionCell)
      node
    }
  }
}

class LocalVariableParser(ctx: StackCtx) extends Parser[LocalVariable] {
  val parser: P[LocalVariable] = P {
    for {
      (start, end, local, name, typ, signature) ←
        "local" ~ WS.? ~ "info" ~ WS.? ~
          Literal ~ WS.? ~ ("->" | "→") ~ WS.? ~ Literal ~ WS.? ~ "," ~ WS.? ~ // range
          Literal ~ WS.? ~ "," ~ WS.? ~ // local
          StringLiteral ~ WS.? ~ "," ~ WS.? ~ // name
          TypeParser ~ WS.? ~ // type
          ("," ~ WS.? ~ StringLiteral ~ WS.?).? ~ // signature
          ";"
    } yield {
      val variable = new LocalVariable(name, typ, Optional.ofNullable(signature.orNull), null, null, ctx.getLocal(local))
      ctx.registerLabelReference(start, variable.getStartCell)
      ctx.registerLabelReference(end, variable.getEndCell)
      variable
    }
  }
}

class LocalVariableAnnotationParser(ctx: StackCtx) extends Parser[LocalVariableAnnotation] {
  val parser: P[LocalVariableAnnotation] = P {
    val locations = {
      val locationParser = P {
        for ((start, end, local) ← Literal ~ WS.? ~ ("->" | "→") ~ WS.? ~ Literal ~ WS.? ~ Literal) yield {
          val location = new LocalVariableAnnotation.Location(null, null, ctx.getLocal(local))
          ctx.registerLabelReference(start, location.getStartCell)
          ctx.registerLabelReference(end, location.getEndCell)
          location
        }
      }

      "[" ~ WS.? ~ locationParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "]"
    }

    for ((locations, annotation) ← "local" ~ WS.? ~ "annotation" ~ WS.? ~ locations.log() ~ WS.? ~ LocalTypeAnnotationParser)
      yield new LocalVariableAnnotation(annotation, locations.asJava)
  }
}