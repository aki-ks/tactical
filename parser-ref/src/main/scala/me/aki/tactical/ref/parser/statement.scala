package me.aki.tactical.ref.parser

import scala.collection.JavaConverters._
import java.util.{ Optional, LinkedHashMap => JLinkedHashMap }
import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.Statement
import me.aki.tactical.ref.condition._
import me.aki.tactical.ref.stmt._

class StatementParser(ctx: UnresolvedRefCtx) extends Parser[Statement] {
  val parser: P [Statement] = P {
    new MonitorEnterStatementParser(ctx) |
    new MonitorExitStatementParser(ctx) |
    new ReturnStatementParser(ctx) |
    new ThrowStatementParser(ctx) |
    new InvokeStatementParser(ctx) |
    new GotoStatementParser(ctx) |
    new IfStatementParser(ctx) |
    new SwitchStatementParser(ctx) |
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

class InvokeStatementParser(ctx: UnresolvedRefCtx) extends Parser[InvokeStmt] {
  val parser: P[InvokeStmt] =
    for (invoke ← new AbstractInvokeParser(ctx) ~ WS.? ~ ";") yield new InvokeStmt(invoke)
}

class GotoStatementParser(ctx: UnresolvedRefCtx) extends Parser[GotoStmt] {
  val parser: P[GotoStmt] =
    for (label ← "goto" ~ WS.? ~ Literal ~ WS.? ~ ";") yield {
      val stmt = new GotoStmt(null)
      ctx.registerReference(label, stmt.getTargetCell)
      stmt
    }
}

class AssignStatementParser(ctx: UnresolvedRefCtx) extends Parser[AssignStatement] {
  val parser: P[AssignStatement] =
    for ((variable, value) ← new VariableParser(ctx) ~ WS.? ~ "=" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ ";")
      yield new AssignStatement(variable, value)
}

class IfStatementParser(ctx: UnresolvedRefCtx) extends Parser[IfStmt] {
  val parser: P[IfStmt] = P {
    val expr = new ExpressionParser(ctx)
    val condition = P {
      P { for (_ ← "==".!) yield new Equal(_, _) } |
      P { for (_ ← "!=".!) yield new NonEqual(_, _) } |
      P { for (_ ← "<=".!) yield new LessEqual(_, _) } |
      P { for (_ ← "<".!) yield new LessThan(_, _) } |
      P { for (_ ← ">=".!) yield new GreaterEqual(_, _) } |
      P { for (_ ← ">".!) yield new GreaterThan(_, _) }
    }

    for ((op1, condition, op2, label) ← "if" ~ WS.? ~ "(" ~ WS.? ~ expr ~ WS.? ~ condition ~ WS.? ~ expr ~ WS.? ~ ")" ~ WS.? ~ "goto" ~ WS.? ~ Literal ~ WS.? ~ ";") yield {
      val stmt = new IfStmt(condition(op1, op2), null)
      ctx.registerReference(label, stmt.getTargetCell)
      stmt
    }
  }
}

class SwitchStatementParser(ctx: UnresolvedRefCtx) extends Parser[SwitchStmt] {
  val parser: P[SwitchStmt] = for {
    (value, cases, default) ←
    "switch" ~ WS.? ~ "(" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ ")" ~ WS.? ~ "{" ~ WS.? ~
      ("case" ~ WS.? ~ int ~ WS.? ~ ":" ~ WS.? ~ "goto" ~ WS.? ~ Literal ~ WS.? ~ ";").rep(sep = WS.?) ~ WS.? ~
      "default" ~ WS.? ~ ":" ~ WS.? ~ "goto" ~ WS.? ~ Literal ~ WS.? ~ ";" ~ WS.? ~
      "}"
  } yield {
    val nullBranchTable = new JLinkedHashMap[Integer, Statement](cases.map { case (key, _) => (key: Integer, null: Statement) }.toMap.asJava)
    val stmt = new SwitchStmt(value, nullBranchTable, null)

    for ((key, label) ← cases) ctx.registerReference(label, stmt.getBranchTableCell(key))
    ctx.registerReference(default, stmt.getDefaultTargetCell)

    stmt
  }
}
