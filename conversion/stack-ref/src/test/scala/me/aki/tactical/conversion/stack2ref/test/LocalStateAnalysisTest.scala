package me.aki.tactical.conversion.stack2ref.test

import java.util.Optional

import scala.collection.JavaConverters._
import me.aki.tactical.conversion.stack2ref.{CfgUnitGraph, LocalStateAnalysis}
import me.aki.tactical.conversion.stack2ref.LocalStateAnalysis.State
import me.aki.tactical.core.`type`.{IntType, ObjectType, Type}
import me.aki.tactical.core.{MethodRef, Path}
import me.aki.tactical.core.constant.{IntConstant, StringConstant}
import me.aki.tactical.ref.condition.Equal
import me.aki.tactical.ref.expr.ConstantExpr
import me.aki.tactical.ref.invoke.InvokeStatic
import me.aki.tactical.ref.stmt.{AssignStmt, IfStmt, InvokeStmt, ReturnStmt}
import me.aki.tactical.ref.{Expression, RefBody, RefLocal}
import org.scalatest.{FlatSpec, Matchers}

class LocalStateAnalysisTest extends FlatSpec with Matchers {

  val parseIntMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](ObjectType.STRING).asJava, Optional.of(IntType.getInstance))

  "The LocalStateAnalysis" should "correctly analyse basic code without branches" in {
    val body = new RefBody()
    val local1 = new RefLocal(null)

    val stmt1 = new AssignStmt(local1, new ConstantExpr(new StringConstant("A")))
    val stmt2 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt3 = new AssignStmt(local1, new ConstantExpr(new StringConstant("B")))
    val stmt4 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt5 = new AssignStmt(local1, new ConstantExpr(new StringConstant("C")))
    val stmt6 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt7 = new ReturnStmt()

    body.getStatements.addAll(stmt1 :: stmt2 :: stmt3 :: stmt4 :: stmt5 :: stmt6 :: stmt7 :: Nil asJava)
    body.getLocals.add(local1)

    val graph = new CfgUnitGraph(body)
    val analysis = new LocalStateAnalysis(graph).getLocalStates(local1)
    def stmtState(stmt: AssignStmt) = new State.Stmt(graph.getNode(body.getStatements.getNext(stmt)), stmt)

    analysis.getStates(stmt2) shouldEqual Set(stmtState(stmt1)).asJava
    analysis.getStates(stmt4) shouldEqual Set(stmtState(stmt3)).asJava
    analysis.getStates(stmt6) shouldEqual Set(stmtState(stmt5)).asJava

    analysis.getGroups.asScala.toSet shouldEqual Set(
      Set(stmtState(stmt1)).asJava,
      Set(stmtState(stmt3)).asJava,
      Set(stmtState(stmt5)).asJava
    )
  }

  it should "correctly analyse basic code using parameter locals" in {
    val body = new RefBody()

    val local1 = new RefLocal(null)

    val stmt1 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt2 = new AssignStmt(local1, new ConstantExpr(new StringConstant("B")))
    val stmt3 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt4 = new AssignStmt(local1, new ConstantExpr(new StringConstant("C")))
    val stmt5 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt6 = new ReturnStmt()

    body.getStatements.addAll(stmt1 :: stmt2 :: stmt3 :: stmt4 :: stmt5 :: stmt6 :: Nil asJava)
    body.getArgumentLocals.add(local1)
    body.getLocals.add(local1)

    val graph = new CfgUnitGraph(body)
    val analysis = new LocalStateAnalysis(graph).getLocalStates(local1)
    def stmtState(stmt: AssignStmt) = new State.Stmt(graph.getNode(body.getStatements.getNext(stmt)), stmt)
    def paramState(i: Int) = new State.Parameter(graph.getHead, i)

    analysis.getStates(stmt1) shouldEqual Set(paramState(0)).asJava
    analysis.getStates(stmt3) shouldEqual Set(stmtState(stmt2)).asJava
    analysis.getStates(stmt5) shouldEqual Set(stmtState(stmt4)).asJava

    analysis.getGroups.asScala.toSet shouldEqual Set(
      Set(paramState(0)).asJava,
      Set(stmtState(stmt2)).asJava,
      Set(stmtState(stmt4)).asJava
    )
  }

  it should "correctly analyse parameter locals with a basic branch" in {
    val body = new RefBody()

    val local1 = new RefLocal(null)
    val local2 = new RefLocal(null)

    val stmt1 = new IfStmt(new Equal(new ConstantExpr(new IntConstant(0)), new ConstantExpr(new IntConstant(1))), null)
    val stmt2 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt3 = new AssignStmt(local1, new ConstantExpr(new StringConstant("A")))
    val stmt4 = new InvokeStmt(new InvokeStatic(parseIntMethod, List[Expression](local1).asJava, false))
    val stmt5 = new ReturnStmt()
    stmt1.setTarget(stmt4)

    body.getStatements.addAll(stmt1 :: stmt2 :: stmt3 :: stmt4 :: stmt5 :: Nil asJava)
    body.getArgumentLocals.add(local1)
    body.getLocals.addAll(local1 :: local2 :: Nil asJava)

    val graph = new CfgUnitGraph(body)
    val analysis = new LocalStateAnalysis(graph).getLocalStates(local1)
    def stmtState(stmt: AssignStmt) = new State.Stmt(graph.getNode(body.getStatements.getNext(stmt)), stmt)
    def paramState(i: Int) = new State.Parameter(graph.getHead, i)

    analysis.getStates(stmt2) shouldEqual Set(paramState(0)).asJava
    analysis.getStates(stmt4) shouldEqual Set(stmtState(stmt3), paramState(0)).asJava
    analysis.getGroups.asScala.toSet shouldEqual Set(Set(paramState(0), stmtState(stmt3)).asJava)
  }
}
