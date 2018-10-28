package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.{MethodDescriptor, MethodRef, Path}
import me.aki.tactical.core.`type`.{IntType, ObjectType, Type}
import me.aki.tactical.core.constant.{BootstrapConstant, ClassConstant, IntConstant, StringConstant}
import me.aki.tactical.core.handle.InvokeStaticHandle
import me.aki.tactical.core.parser.Parser
import me.aki.tactical.ref.invoke.{InvokeDynamic, InvokeSpecial, InvokeStatic}
import me.aki.tactical.ref.{Expression, Statement}
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

  it should "parse regular invoke statements" in {
    stmt.parse("java.lang.String.<static>.valueOf(local1 : int) : java.lang.String;") shouldEqual
      new InvokeStmt(new InvokeStatic(new MethodRef(Path.STRING, "valueOf", List[Type](IntType.getInstance).asJava, Optional.of(ObjectType.STRING)), List[Expression](local1).asJava, false))

    stmt.parse("java.lang.String.<special interface local1>.valueOf(local2 : int) : java.lang.String;") shouldEqual
      new InvokeStmt(new InvokeSpecial(new MethodRef(Path.STRING, "valueOf", List[Type](IntType.getInstance).asJava, Optional.of(ObjectType.STRING)), local1, List[Expression](local2).asJava, true))
  }

  it should "parse dynamic invokes" in {
    val invoke = stmt.parse(
      """invoke dynamic {
        |    name = "foo",
        |    type = (local1 : int, local2 : java.lang.String) : void,
        |    bootstrap = invoke static java.lang.Integer.parseInt(java.lang.String) : int,
        |    arguments = [ 10, "foo", java.lang.String.class ]
        |};
      """.stripMargin)

    val typ = new MethodDescriptor(List(IntType.getInstance, ObjectType.STRING).asJava, Optional.empty())
    val bootstrapMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance))
    val bootstrap = new InvokeStaticHandle(bootstrapMethod, false)
    val bootstrapArguments = List[BootstrapConstant](new IntConstant(10), new StringConstant("foo"), new ClassConstant(new ObjectType(Path.STRING))).asJava
    val arguments = List[Expression](local1, local2).asJava
    invoke shouldEqual new InvokeStmt(new InvokeDynamic("foo", typ, bootstrap, bootstrapArguments, arguments))
  }
}
