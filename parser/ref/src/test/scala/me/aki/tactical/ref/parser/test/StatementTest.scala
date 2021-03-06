package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._
import java.util.{ Optional, LinkedHashMap => JLinkedHashMap }
import fastparse.all._
import me.aki.tactical.core.{MethodDescriptor, MethodRef, Path}
import me.aki.tactical.core.`type`.{IntType, ObjectType, Type}
import me.aki.tactical.core.constant.{BootstrapConstant, ClassConstant, IntConstant, StringConstant}
import me.aki.tactical.core.handle.InvokeStaticHandle
import me.aki.tactical.core.parser.Parser
import me.aki.tactical.ref.condition.{Equal, LessThan}
import me.aki.tactical.ref.invoke.{InvokeDynamic, InvokeSpecial, InvokeStatic}
import me.aki.tactical.ref.{Expression, Statement}
import me.aki.tactical.ref.parser.StatementParser
import me.aki.tactical.ref.stmt._

class StatementTest extends AbstractUnresolvedCtxTest {
  def parseStmt(f: Parser[Statement] => Statement): Statement = {
    val ctx = parseCtx
    val statementParser = new Parser[Statement] {
      val parser: P[Statement] = Start ~ new StatementParser(ctx) ~ End
    }
    val stmt = f(statementParser)
    ctx.resolve(labels)
    stmt
  }


  "The StatementParser" should "parse monitor enter statements" in {
    parseStmt(_.parse("monitor enter local1;")) match {
      case stmt: MonitorEnterStmt =>
        stmt.getValue shouldEqual local1
    }
  }

  it should "parse monitor exit statements" in {
    parseStmt(_.parse("monitor exit local1;")) match {
      case stmt: MonitorExitStmt =>
        stmt.getValue shouldEqual local1
    }
  }

  it should "parse return statements" in {
    parseStmt(_.parse("return local1;")) match {
      case stmt: ReturnStmt =>
        stmt.getValue shouldEqual Optional.of(local1)
    }
  }

  it should "parse throw statements" in {
    parseStmt(_.parse("throw local1;")) match {
      case stmt: ThrowStmt =>
        stmt.getValue shouldEqual local1
    }
  }

  it should "parse assignment statements" in {
    parseStmt(_.parse("local1 = local2;")) match {
      case stmt: AssignStmt =>
        stmt.getVariable shouldEqual local1
        stmt.getValue shouldEqual local2
    }
  }

  it should "parse regular invoke statements" in {
    parseStmt(_.parse("java.lang.String.<static>.valueOf(local1 : int) : java.lang.String;")) match {
      case stmt: InvokeStmt =>
        stmt.getInvoke shouldEqual new InvokeStatic(new MethodRef(Path.STRING, "valueOf", List[Type](IntType.getInstance).asJava, Optional.of(ObjectType.STRING)), List[Expression](local1).asJava, false)
    }

    parseStmt(_.parse("java.lang.String.<special interface local1>.valueOf(local2 : int) : java.lang.String;")) match {
      case stmt: InvokeStmt =>
        stmt.getInvoke shouldEqual new InvokeSpecial(new MethodRef(Path.STRING, "valueOf", List[Type](IntType.getInstance).asJava, Optional.of(ObjectType.STRING)), local1, List[Expression](local2).asJava, true)
    }
  }

  it should "parse dynamic invokes" in {
    parseStmt(_.parse(
      """invoke dynamic {
        |    name = "foo",
        |    type = (local1 : int, local2 : java.lang.String) : void,
        |    bootstrap = invoke static java.lang.Integer.parseInt(java.lang.String) : int,
        |    arguments = [ 10, "foo", java.lang.String.class ]
        |};
      """.stripMargin.trim)) match {
      case invoke: InvokeStmt =>
        val typ = new MethodDescriptor(List(IntType.getInstance, ObjectType.STRING).asJava, Optional.empty())
        val bootstrapMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance))
        val bootstrap = new InvokeStaticHandle(bootstrapMethod, false)
        val bootstrapArguments = List[BootstrapConstant](new IntConstant(10), new StringConstant("foo"), new ClassConstant(new ObjectType(Path.STRING))).asJava
        val arguments = List[Expression](local1, local2).asJava
        invoke.getInvoke shouldEqual new InvokeDynamic("foo", typ, bootstrap, bootstrapArguments, arguments)
    }
  }

  it should "parse goto statements" in {
    parseStmt(_.parse("goto label1;")) match {
      case goto: GotoStmt =>
        goto.getTarget shouldEqual label1
    }
  }

  it should "parse if statements" in {
    parseStmt(_.parse("if (local1 == local2) goto label1;")) match {
      case stmt: IfStmt =>
        stmt.getCondition shouldEqual new Equal(local1, local2)
        stmt.getTarget shouldEqual label1
    }
  }

  it should "parse switch statements" in {
    parseStmt(_.parse(
      """switch (local1) {
        |  case 9: goto label1;
        |  case 2: goto label2;
        |  default: goto label3;
        |}
      """.stripMargin.trim)) match {
      case stmt: SwitchStmt =>
        stmt.getValue shouldEqual local1
        stmt.getBranchTable shouldEqual new JLinkedHashMap[Integer, Statement](Map[Integer, Statement]((9, label1), (2, label2)).asJava)
        stmt.getDefaultTarget shouldEqual label3
    }
  }
}
