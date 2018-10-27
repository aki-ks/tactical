package me.aki.tactical.ref.parser

import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.Statement
import me.aki.tactical.ref.stmt.{MonitorEnterStmt, MonitorExitStmt}

class StatementParser(ctx: UnresolvedRefCtx) extends Parser[Statement] {
  val parser: P [Statement] = P {
    new MonitorEnterStatementParser(ctx) |
    new MonitorExitStatementParser(ctx)
  }
}

class MonitorEnterStatementParser(ctx: UnresolvedRefCtx) extends Parser[MonitorEnterStmt] {
  val parser: P[MonitorEnterStmt] =
    for (value ← "monitor" ~ WS.? ~ "enter" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.?)
      yield new MonitorEnterStmt(value)
}

class MonitorExitStatementParser(ctx: UnresolvedRefCtx) extends Parser[MonitorExitStmt] {
  val parser: P[MonitorExitStmt] =
    for (value ← "monitor" ~ WS.? ~ "exit" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.?)
      yield new MonitorExitStmt(value)
}
