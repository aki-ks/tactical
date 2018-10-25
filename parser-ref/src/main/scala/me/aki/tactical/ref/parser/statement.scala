package me.aki.tactical.ref.parser

import fastparse.all._
import me.aki.tactical.core.parser.Parser
import me.aki.tactical.ref.Statement

class StatementParser(ctx: UnresolvedRefCtx) extends Parser[Statement] {
  val parser: P [Statement] = P {
    Fail
  }
}
