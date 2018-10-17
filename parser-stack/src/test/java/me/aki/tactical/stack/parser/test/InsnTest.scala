package me.aki.tactical.stack.parser.test

import java.util

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.{FieldRef, MethodDescriptor, MethodRef, Path}
import me.aki.tactical.core.parser.Parser
import me.aki.tactical.core.`type`._
import me.aki.tactical.core.constant._
import me.aki.tactical.core.handle.InvokeStaticHandle
import me.aki.tactical.stack.insn._
import me.aki.tactical.stack.invoke._
import me.aki.tactical.stack.parser.{InsnParser, ResolvedStackCtx, UnresolvedStackCtx}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class InsnTest extends FlatSpec with Matchers with PropertyChecks {
  val label1: Instruction = new PushInsn(new StringConstant("label1"))
  val label2: Instruction = new PushInsn(new StringConstant("label2"))
  val label3: Instruction = new PushInsn(new StringConstant("label3"))
  val label4: Instruction = new PushInsn(new StringConstant("label4"))

  def resolve(ctx: UnresolvedStackCtx): ResolvedStackCtx =
    ctx.resolve(Map(
      "label1" -> label1,
      "label2" -> label2,
      "label3" -> label3,
      "label4" -> label4
    ))

  def parseInsn(text: String, ctx: UnresolvedStackCtx = new UnresolvedStackCtx()): Instruction =
    new Parser[Instruction] {
      val parser: P[Instruction] = Start ~ new InsnParser(ctx) ~ End
    } parse text

  val methodRefs = Table(
    ("textifier MethodRef", "MethodRef"),
    ("java.io.PrintStream.println(int) : void", new MethodRef(Path.of("java", "io", "PrintStream"), "println", List[Type](IntType.getInstance).asJava, Optional.empty[Type])),
    ("java.lang.Integer.parseInt(java.lang.String) : int", new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance)))
  )

  val intLongFloatDouble = Table(
    ("typ as String", "typ instance"),
    ("int", IntType.getInstance()),
    ("long", LongType.getInstance()),
    ("float", FloatType.getInstance()),
    ("double", DoubleType.getInstance()),
  )

  val intLong = Table(
    ("typ as String", "typ instance"),
    ("int", IntType.getInstance()),
    ("long", LongType.getInstance()),
  )

  val floatDouble = Table(
    ("typ as String", "typ instance"),
    ("float", FloatType.getInstance()),
    ("double", DoubleType.getInstance()),
  )

  val primitiveType = Table(
    ("typ as String", "typ instance"),
    ("boolean", BooleanType.getInstance()),
    ("byte", ByteType.getInstance()),
    ("short", ShortType.getInstance()),
    ("char", CharType.getInstance()),
    ("int", IntType.getInstance()),
    ("long", LongType.getInstance()),
    ("float", FloatType.getInstance()),
    ("double", DoubleType.getInstance()),
  )

  "The InsnTextifier" should "parse math instructions" in {
    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"add $typName;") shouldEqual new AddInsn(typ)
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"sub $typName;") shouldEqual new SubInsn(typ)
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"mul $typName;") shouldEqual new MulInsn(typ)
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"div $typName;") shouldEqual new DivInsn(typ)
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"mod $typName;") shouldEqual new ModInsn(typ)
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"and $typName;") shouldEqual new AndInsn(typ)
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"or $typName;") shouldEqual new OrInsn(typ)
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"xor $typName;") shouldEqual new XorInsn(typ)
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"shl $typName;") shouldEqual new ShlInsn(typ)
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"shr $typName;") shouldEqual new ShrInsn(typ)
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"ushr $typName;") shouldEqual new UShrInsn(typ)
    }

    parseInsn(s"cmp;") shouldEqual new CmpInsn()

    forAll (floatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"cmpl $typName;") shouldEqual new CmplInsn(typ)
    }

    forAll (floatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"cmpg $typName;") shouldEqual new CmpgInsn(typ)
    }
  }

  it should "parse goto instructions" in {
    val ctx = new UnresolvedStackCtx
    val insn = parseInsn("goto label1;", ctx)
    resolve(ctx)

    insn shouldEqual new GotoInsn(label1)
  }

  it should "parse if instructions" in {
    import IfInsn._
    def parseIfInsn(unparsed: String, condition: Condition, label: Instruction): Unit = {
      val ctx = new UnresolvedStackCtx
      val insn = parseInsn(unparsed, ctx)
      resolve(ctx)

      insn shouldEqual new IfInsn(condition, label)
    }

    parseIfInsn("if ref (? == null) goto label1;", IF_NULL, label1)
    parseIfInsn("if ref (? != null) goto label2;", IF_NONNULL, label2)

    parseIfInsn("if ref (? == ?) goto label1;", IF_REF_EQ, label1)
    parseIfInsn("if ref (? != ?) goto label2;", IF_REF_NE, label2)

    parseIfInsn("if int (? == ?) goto label1;", IF_INT_EQ, label1)
    parseIfInsn("if int (? != ?) goto label2;", IF_INT_NE, label2)
    parseIfInsn("if int (? < ?) goto label3;", IF_INT_LT, label3)
    parseIfInsn("if int (? <= ?) goto label3;", IF_INT_LE, label3)
    parseIfInsn("if int (? > ?) goto label3;", IF_INT_GT, label3)
    parseIfInsn("if int (? >= ?) goto label3;", IF_INT_GE, label3)

    parseIfInsn("if int (? == 0) goto label1;", IF_EQ_ZERO, label1)
    parseIfInsn("if int (? != 0) goto label2;", IF_NE_ZERO, label2)
    parseIfInsn("if int (? < 0) goto label3;", IF_LT_ZERO, label3)
    parseIfInsn("if int (? <= 0) goto label3;", IF_LE_ZERO, label3)
    parseIfInsn("if int (? > 0) goto label3;", IF_GT_ZERO, label3)
    parseIfInsn("if int (? >= 0) goto label3;", IF_GE_ZERO, label3)
  }

  it should "parse switch instructions" in {
    val ctx = new UnresolvedStackCtx
    val insn = parseInsn(
      """switch {
        |  case 0 : goto label1 ;
        |  case 1: goto label2;
        |  default: goto label3;
        |}
      """.stripMargin.trim, ctx)
    resolve(ctx)

    val table = new util.LinkedHashMap[Integer, Instruction]()
    table.put(0, label1)
    table.put(1, label2)
    insn shouldEqual new SwitchInsn(table, label3)
  }

  it should "parse field get and sets" in {
    parseInsn("get example.Test.foo : int;") shouldEqual
      new FieldGetInsn(new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance), false)

    parseInsn("get static example.Test.foo : int;") shouldEqual
      new FieldGetInsn(new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance), true)

    parseInsn("set example.Test.foo : int;") shouldEqual
      new FieldSetInsn(new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance), false)

    parseInsn("set static example.Test.foo : int;") shouldEqual
      new FieldSetInsn(new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance), true)
  }

  it should "parse array length insns" in {
    parseInsn("arraylength;") shouldEqual new ArrayLengthInsn()
    parseInsn("arraylength ;") shouldEqual new ArrayLengthInsn()
  }

  it should "parse array load insns" in {
    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"arrayload $typString;") shouldEqual new ArrayLoadInsn(typ)
    }
  }

  it should "parse array store insns" in {
    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"arraystore $typString;") shouldEqual new ArrayStoreInsn(typ)
    }
  }

  it should "parse increment insns" in {
    val ctx = new UnresolvedStackCtx()
    val local = ctx.getLocal("local")

    forAll { int: Int =>
      parseInsn(s"inc local $int;", ctx) shouldEqual new IncrementInsn(local, int)
    }
  }

  it should "parse instanceof insns" in {
    parseInsn(s"instanceof java.lang.String;") shouldEqual
      new InstanceOfInsn(new ObjectType(Path.of("java", "lang", "String")))
  }

  it should "parse regular invokes" in {
    forAll (methodRefs) { (textified, methodRef) =>
      parseInsn(s"invoke interface $textified;") shouldEqual new InvokeInsn(new InterfaceInvoke(methodRef))
      parseInsn(s"invoke virtual $textified;") shouldEqual new InvokeInsn(new VirtualInvoke(methodRef))
      parseInsn(s"invoke special $textified;") shouldEqual new InvokeInsn(new SpecialInvoke(methodRef, false))
      parseInsn(s"invoke static $textified;") shouldEqual new InvokeInsn(new StaticInvoke(methodRef, false))
    }
  }

  it should "parse static/special invokes of methods in interfaces" in {
    forAll (methodRefs) { (textified, methodRef) =>
      parseInsn(s"invoke special interface $textified;") shouldEqual new InvokeInsn(new SpecialInvoke(methodRef, true))
      parseInsn(s"invoke static interface $textified;") shouldEqual new InvokeInsn(new StaticInvoke(methodRef, true))
    }
  }

  it should "parse invoke dynamic instructions" in {
    val insn = parseInsn(
      """invoke dynamic {
        |  name = "foo",
        |  type = (int, long) void,
        |  bootstrap = invoke static java.lang.Integer.parseInt(java.lang.String) : int,
        |  arguments = [ 10, "foo", java.lang.String.class ]
        |};
      """.stripMargin.trim)

    val desc = new MethodDescriptor(List[Type](IntType.getInstance, LongType.getInstance).asJava, Optional.empty())
    val bootstrapMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance))
    val bootstrap = new InvokeStaticHandle(bootstrapMethod, false)
    val arguments = List[BootstrapConstant](new IntConstant(10), new StringConstant("foo"), new ClassConstant(new ObjectType(Path.STRING))).asJava
    insn shouldEqual new InvokeInsn(new DynamicInvoke("foo", desc, bootstrap, arguments))
  }

  it should "parse load instructions" in {
    val ctx = new UnresolvedStackCtx()
    val local = ctx.getLocal("local")

    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"load $typString local;", ctx) shouldEqual new LoadInsn(typ, local)
    }
  }

  it should "parse store instructions" in {
    val ctx = new UnresolvedStackCtx()
    val local = ctx.getLocal("local")

    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"store $typString local;", ctx) shouldEqual new StoreInsn(typ, local)
    }
  }

  it should "parse monitor enter instructions" in {
    parseInsn(s"monitor enter;") shouldEqual new MonitorEnterInsn
  }

  it should "parse monitor exit instructions" in {
    parseInsn(s"monitor exit;") shouldEqual new MonitorExitInsn
  }

  it should "parse neg insn" in {
    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"neg $typString;") shouldEqual new NegInsn(typ)
    }
  }

  it should "parse new array insns" in {
    parseInsn("new int[?];") shouldEqual new NewArrayInsn(new ArrayType(IntType.getInstance, 1))
    parseInsn("new int[?][];") shouldEqual new NewArrayInsn(new ArrayType(IntType.getInstance, 2))
    parseInsn("new int[?][?][?];") shouldEqual new NewArrayInsn(new ArrayType(IntType.getInstance, 3), 3)
  }

  it should "parse 'new' instructions" in {
    parseInsn("new java.lang.String;") shouldEqual new NewInsn(Path.STRING)
  }

  it should "parse primitive cast instructions" in {
    forAll (intLongFloatDouble) { (fromTypeString, fromType) =>
      forAll(primitiveType) { (toTypeString, toType) =>
        whenever(!toType.isInstanceOf[BooleanType]) {
          parseInsn(s"cast $fromTypeString -> $toTypeString;") shouldEqual new PrimitiveCastInsn(fromType, toType)
        }
      }
    }
  }

  it should "parse ref cast instructions" in {
    parseInsn(s"cast java.lang.String;") shouldEqual new RefCastInsn(new ObjectType(Path.STRING))
    parseInsn(s"cast java.lang.String[];") shouldEqual new RefCastInsn(new ArrayType(new ObjectType(Path.STRING), 1))
  }

  it should "parse push instructions" in {
    parseInsn("push 10;") shouldEqual new PushInsn(new IntConstant(10))
    parseInsn("push 10L;") shouldEqual new PushInsn(new LongConstant(10))
    parseInsn("push java.lang.String.class;") shouldEqual new PushInsn(new ClassConstant(new ObjectType(Path.STRING)))
  }

  it should "parse return instructions" in {
    parseInsn("return;") shouldEqual new ReturnInsn()
    parseInsn("return int;") shouldEqual new ReturnInsn(IntType.getInstance)
    assert (parseInsn("return ref;").asInstanceOf[ReturnInsn].getType.get.isInstanceOf[RefType])
  }

  it should "parse throw instruction" in {
    parseInsn("throw;") shouldEqual new ThrowInsn
    parseInsn("throw ;") shouldEqual new ThrowInsn
  }

  it should "parse swap instructions" in {
    parseInsn("swap;") shouldEqual new SwapInsn
    parseInsn("swap ;") shouldEqual new SwapInsn
  }

  it should "parse pop instructions" in {
    parseInsn("pop;") shouldEqual new PopInsn
    parseInsn("pop ;") shouldEqual new PopInsn
  }

  it should "parse all kinds of dup instructions" in {
    parseInsn("dup;") shouldEqual new DupInsn
    parseInsn("dup x1;") shouldEqual new DupX1Insn
    parseInsn("dup x2;") shouldEqual new DupX2Insn
    parseInsn("dup2;") shouldEqual new Dup2Insn
    parseInsn("dup2 x1;") shouldEqual new Dup2X1Insn
    parseInsn("dup2 x2;") shouldEqual new Dup2X2Insn
  }
}
