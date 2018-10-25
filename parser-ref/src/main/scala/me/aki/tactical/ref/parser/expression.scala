package me.aki.tactical.ref.parser

import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.Expression

class ExpressionParser(ctx: UnresolvedRefCtx) extends Parser[Expression] {
  val parse: P[Expression] = Fail
}
