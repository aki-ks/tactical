package me.aki.tactical.ref.parser.test

import fastparse.all._
import me.aki.tactical.core.parser.Parser
import me.aki.tactical.ref.Statement
import me.aki.tactical.ref.parser.StatementParser
import me.aki.tactical.ref.stmt.{MonitorEnterStmt, MonitorExitStmt}

class StatementTest extends AbstractUnresolvedCtxTest {
  val stmt = new Parser[Statement] {
    val parser: P[Statement] = new StatementParser(parseCtx)
  }

  "The StatementParser" should "parse monitor enter statements" in {
    stmt.parse("monitor enter local1") shouldEqual new MonitorEnterStmt(local1)
  }

  it should "parse monitor exit statements" in {
    stmt.parse("monitor exit local1") shouldEqual new MonitorExitStmt(local1)
  }

}
