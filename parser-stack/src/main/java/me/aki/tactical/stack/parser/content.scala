package me.aki.tactical.stack.parser

import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.stack.StackBody.{LineNumber, LocalVariable}
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
