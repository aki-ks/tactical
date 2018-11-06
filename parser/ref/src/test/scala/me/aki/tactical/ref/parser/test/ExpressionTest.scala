package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._

import me.aki.tactical.core.{FieldRef, MethodDescriptor, MethodRef, Path}
import me.aki.tactical.core.`type`.{ArrayType, IntType, ObjectType, Type}
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.core.constant._
import me.aki.tactical.core.handle.InvokeStaticHandle
import me.aki.tactical.ref.Expression
import me.aki.tactical.ref.expr._
import me.aki.tactical.ref.invoke.{InvokeDynamic, InvokeSpecial, InvokeStatic}
import me.aki.tactical.ref.parser.ExpressionParser
import org.scalacheck.Gen

class ExpressionTest extends AbstractUnresolvedCtxTest {
  val expr = new Parser[Expression] {
    val parser: P[Expression] = Start ~ new ExpressionParser(parseCtx) ~ End
  }

  "The ExpressionParser" should "parse all kinds of constants" in {
    expr.parse("10") shouldEqual new ConstantExpr(new IntConstant(10))
    expr.parse("20L") shouldEqual new ConstantExpr(new LongConstant(20))
  }

  it should "parse all kinds of neg expressions" in {
    expr.parse("-(20)") shouldEqual new NegExpr(new ConstantExpr(new IntConstant(20)))
    expr.parse("-local1") shouldEqual new NegExpr(local1)
  }

  it should "parse new expressions" in {
    expr.parse("new java.lang.String") shouldEqual new NewExpr(Path.STRING)
    expr.parse("new java.lang.Throwable") shouldEqual new NewExpr(Path.THROWABLE)
  }

  it should "parse one-dimensional array initializations" in {
    expr.parse("new int[3]") shouldEqual
      new NewArrayExpr(new ArrayType(IntType.getInstance, 1), List[Expression](new ConstantExpr(new IntConstant(3))).asJava)
  }

  it should "parse simple multi-dimensional array initializations" in {
    expr.parse("new int[3][]") shouldEqual
      new NewArrayExpr(new ArrayType(IntType.getInstance, 2), List[Expression](new ConstantExpr(new IntConstant(3))).asJava)
  }

  it should "parse multi-dimensional array initializations with multiple initilized dimensions" in {
    expr.parse("new int[3][2][9]") shouldEqual
      new NewArrayExpr(new ArrayType(IntType.getInstance, 3), List[Expression](
        new ConstantExpr(new IntConstant(3)),
        new ConstantExpr(new IntConstant(2)),
        new ConstantExpr(new IntConstant(9))).asJava)
  }

  it should "parse all kinds of casts" in {
    expr.parse("(int) local1") shouldEqual new CastExpr(IntType.getInstance, local1)
    expr.parse("(java.lang.String) local1") shouldEqual new CastExpr(ObjectType.STRING, local1)
  }

  it should "parse regular invokes" in {
    expr.parse("java.lang.String.<static>.valueOf(local1 : int) : java.lang.String") shouldEqual
      new InvokeExpr(new InvokeStatic(new MethodRef(Path.STRING, "valueOf", List[Type](IntType.getInstance).asJava, Optional.of(ObjectType.STRING)), List[Expression](local1).asJava, false))

    expr.parse("java.lang.String.<special interface local1>.valueOf(local2 : int) : java.lang.String") shouldEqual
      new InvokeExpr(new InvokeSpecial(new MethodRef(Path.STRING, "valueOf", List[Type](IntType.getInstance).asJava, Optional.of(ObjectType.STRING)), local1, List[Expression](local2).asJava, true))
  }

  it should "parse dynamic invokes" in {
    val invoke = expr.parse(
      """invoke dynamic {
        |    name = "foo",
        |    type = (local1 : int, local2 : java.lang.String) : void,
        |    bootstrap = invoke static java.lang.Integer.parseInt(java.lang.String) : int,
        |    arguments = [ 10, "foo", java.lang.String.class ]
        |}
      """.stripMargin)

    val typ = new MethodDescriptor(List(IntType.getInstance, ObjectType.STRING).asJava, Optional.empty())
    val bootstrapMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance))
    val bootstrap = new InvokeStaticHandle(bootstrapMethod, false)
    val bootstrapArguments = List[BootstrapConstant](new IntConstant(10), new StringConstant("foo"), new ClassConstant(new ObjectType(Path.STRING))).asJava
    val arguments = List[Expression](local1, local2).asJava
    invoke shouldEqual new InvokeExpr(new InvokeDynamic("foo", typ, bootstrap, bootstrapArguments, arguments))
  }

  it should "parse static field expressions" in {
    expr.parse("java.lang.System.out : java.io.PrintStream") shouldEqual
      new StaticFieldExpr(new FieldRef(Path.of("java", "lang", "System"), "out", new ObjectType(Path.of("java", "io", "PrintStream"))))
  }

  it should "parse instance field expressions" in {
    expr.parse("java.lang.System.<local1>.out : java.io.PrintStream") shouldEqual
      new InstanceFieldExpr(new FieldRef(Path.of("java", "lang", "System"), "out", new ObjectType(Path.of("java", "io", "PrintStream"))), local1)
  }

  it should "parse all kinds of math instructions" in {
    val operations: Gen[(String, (Expression, Expression) => AbstractBinaryExpr)] = Gen.oneOf(
      ("+", new AddExpr(_, _)),
      ("-", new SubExpr(_, _)),
      ("*", new MulExpr(_, _)),
      ("/", new DivExpr(_, _)),
      ("%", new ModExpr(_, _)),
      ("&", new AndExpr(_, _)),
      ("|", new OrExpr(_, _)),
      ("^", new XorExpr(_, _)),
      ("<<", new ShlExpr(_, _)),
      (">>", new ShrExpr(_, _)),
      (">>>", new UShrExpr(_, _)),
      ("cmp", new CmpExpr(_, _)),
      ("cmpl", new CmplExpr(_, _)),
      ("cmpg", new CmpgExpr(_, _))
    )

    forAll (operations) { case (symbol, apply) =>
      expr.parse(s"10 $symbol 20") shouldEqual apply(new ConstantExpr(new IntConstant(10)), new ConstantExpr(new IntConstant(20)))
    }
  }

  it should "parse instanceof expressions" in {
    expr.parse("local1 instanceof java.lang.String") shouldEqual new InstanceOfExpr(ObjectType.STRING, local1)
    expr.parse("local1 instanceof java.lang.String[][]") shouldEqual new InstanceOfExpr(new ArrayType(ObjectType.STRING, 2), local1)
  }

  it should "parse array box expressions" in {
    expr.parse("local1[0]") shouldEqual new ArrayBoxExpr(local1, new ConstantExpr(new IntConstant(0)))
  }

  it should "parse array length expressions" in {
    expr.parse("local1.length") shouldEqual new ArrayLengthExpr(local1)
  }
}
