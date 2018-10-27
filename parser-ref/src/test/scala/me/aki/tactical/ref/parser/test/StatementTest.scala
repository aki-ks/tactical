package me.aki.tactical.ref.parser.test

import fastparse.all._
import me.aki.tactical.core.parser.Parser
import me.aki.tactical.ref.Statement
import me.aki.tactical.ref.parser.StatementParser
import me.aki.tactical.ref.stmt._

class StatementTest extends AbstractUnresolvedCtxTest {
  val stmt = new Parser[Statement] {
    val parser: P[Statement] = new StatementParser(parseCtx)
  }

  "The StatementParser" should "parse monitor enter statements" in {
    stmt.parse("monitor enter local1;") shouldEqual new MonitorEnterStmt(local1)
  }

  it should "parse monitor exit statements" in {
    stmt.parse("monitor exit local1;") shouldEqual new MonitorExitStmt(local1)
  }

  it should "parse return statements" in {
    stmt.parse("return local1;") shouldEqual new ReturnStmt(local1)
  }

  it should "parse throw statements" in {
    stmt.parse("throw local1;") shouldEqual new ThrowStmt(local1)
  }

  it should "parse assignment statements" in {
    stmt.parse("local1 = local2;") shouldEqual new AssignStatement(local1, local2)
  }
}
