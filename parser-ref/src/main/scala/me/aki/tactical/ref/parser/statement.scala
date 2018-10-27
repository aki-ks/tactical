package me.aki.tactical.ref.parser

import java.util.Optional

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.Statement
import me.aki.tactical.ref.stmt._

class StatementParser(ctx: UnresolvedRefCtx) extends Parser[Statement] {
  val parser: P [Statement] = P {
    new MonitorEnterStatementParser(ctx) |
    new MonitorExitStatementParser(ctx) |
    new ReturnStatementParser(ctx) |
    new ThrowStatementParser(ctx) |
    new AssignStatementParser(ctx)
  }
}

class MonitorEnterStatementParser(ctx: UnresolvedRefCtx) extends Parser[MonitorEnterStmt] {
  val parser: P[MonitorEnterStmt] =
    for (value ← "monitor" ~ WS.? ~ "enter" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ ";")
      yield new MonitorEnterStmt(value)
}

class MonitorExitStatementParser(ctx: UnresolvedRefCtx) extends Parser[MonitorExitStmt] {
  val parser: P[MonitorExitStmt] =
    for (value ← "monitor" ~ WS.? ~ "exit" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ ";")
      yield new MonitorExitStmt(value)
}

class ReturnStatementParser(ctx : UnresolvedRefCtx) extends Parser[ReturnStmt] {
  val parser: P[ReturnStmt] =
    for (value ← "return" ~ WS.? ~ (new ExpressionParser(ctx) ~ WS.?).? ~ ";")
      yield new ReturnStmt(Optional.ofNullable(value.orNull))
}

class ThrowStatementParser(ctx: UnresolvedRefCtx) extends Parser[ThrowStmt] {
  val parser: P[ThrowStmt] =
    for (value ← "throw" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ ";") yield new ThrowStmt(value)
}

class AssignStatementParser(ctx: UnresolvedRefCtx) extends Parser[AssignStatement] {
  val parser: P[AssignStatement] =
    for ((variable, value) ← new VariableParser(ctx) ~ WS.? ~ "=" ~ WS.? ~ new ExpressionParser(ctx))
      yield new AssignStatement(variable, value)
}
