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
      parseInsn(s"add $typName;") match {
        case insn: AddInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"sub $typName;") match {
        case insn: SubInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"mul $typName;") match {
        case insn: MulInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"div $typName;") match {
        case insn: DivInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLongFloatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"mod $typName;") match {
        case insn: ModInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"and $typName;") match {
        case insn: AndInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"or $typName;") match {
        case insn: OrInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"xor $typName;") match {
        case insn: XorInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"shl $typName;") match {
        case insn: ShlInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"shr $typName;") match {
        case insn: ShrInsn => insn.getType shouldEqual typ
      }
    }

    forAll (intLong) { (typName: String, typ: Type) =>
      parseInsn(s"ushr $typName;") match {
        case insn: UShrInsn => insn.getType shouldEqual typ
      }
    }

    assert(parseInsn(s"cmp;").isInstanceOf[CmpInsn])

    forAll (floatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"cmpl $typName;") match {
        case insn: CmplInsn => insn.getType shouldEqual typ
      }
    }

    forAll (floatDouble) { (typName: String, typ: Type) =>
      parseInsn(s"cmpg $typName;") match {
        case insn: CmpgInsn => insn.getType shouldEqual typ
      }
    }
  }

  it should "parse goto instructions" in {
    val ctx = new UnresolvedStackCtx
    val insn = parseInsn("goto label1;", ctx)
    resolve(ctx)

    insn match {
      case insn: GotoInsn => insn.getTarget shouldEqual label1
    }
  }

  it should "parse if instructions" in {
    import IfInsn._
    def parseIfInsn(unparsed: String, condition: Condition, label: Instruction): Unit = {
      val ctx = new UnresolvedStackCtx
      val insn = parseInsn(unparsed, ctx)
      resolve(ctx)

      insn match {
        case insn: IfInsn =>
          insn.getCondition shouldEqual condition
          insn.getTarget shouldEqual label
      }
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

    insn match {
      case insn: SwitchInsn =>
        val table = new util.LinkedHashMap[Integer, Instruction]()
        table.put(0, label1)
        table.put(1, label2)

        insn.getBranchTable shouldEqual table
        insn.getDefaultLocation shouldEqual label3
    }
  }

  it should "parse field get and sets" in {
    parseInsn("get example.Test.foo : int;") match {
      case insn: FieldGetInsn =>
        insn.getField shouldEqual new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance)
        insn.isStatic shouldEqual false
    }

    parseInsn("get static example.Test.foo : int;") match {
      case insn: FieldGetInsn =>
        insn.getField shouldEqual new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance)
        insn.isStatic shouldEqual true
    }

    parseInsn("set example.Test.foo : int;") match {
      case insn: FieldSetInsn =>
        insn.getField shouldEqual new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance)
        insn.isStatic shouldEqual false
    }

    parseInsn("set static example.Test.foo : int;") match {
      case insn: FieldSetInsn =>
        insn.getField shouldEqual new FieldRef(Path.of("example", "Test"), "foo", IntType.getInstance)
        insn.isStatic shouldEqual true
    }
  }

  it should "parse array length insns" in {
    assert(parseInsn("arraylength;").isInstanceOf[ArrayLengthInsn])
    assert(parseInsn("arraylength ;").isInstanceOf[ArrayLengthInsn])
  }

  it should "parse array load insns" in {
    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"arrayload $typString;") match {
        case insn: ArrayLoadInsn => insn.getType shouldEqual typ
      }
    }
  }

  it should "parse array store insns" in {
    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"arraystore $typString;") match {
        case insn: ArrayStoreInsn => insn.getType shouldEqual typ
      }
    }
  }

  it should "parse increment insns" in {
    val ctx = new UnresolvedStackCtx()
    val local = ctx.getLocal("local")

    forAll { int: Int =>
      parseInsn(s"inc local $int;", ctx) match {
        case insn: IncrementInsn =>
          insn.getLocal shouldEqual local
          insn.getValue shouldEqual int
      }
    }
  }

  it should "parse instanceof insns" in {
    parseInsn(s"instanceof java.lang.String;") match {
      case insn: InstanceOfInsn => insn.getType shouldEqual new ObjectType(Path.STRING)
    }
  }

  it should "parse regular invokes" in {
    forAll (methodRefs) { (textified, methodRef) =>
      parseInsn(s"invoke interface $textified;") match {
        case insn: InvokeInsn => insn.getInvoke shouldEqual new InterfaceInvoke(methodRef)
      }

      parseInsn(s"invoke virtual $textified;") match {
        case insn: InvokeInsn => insn.getInvoke shouldEqual new VirtualInvoke(methodRef)
      }

      parseInsn(s"invoke special $textified;") match {
        case insn: InvokeInsn => insn.getInvoke shouldEqual new SpecialInvoke(methodRef, false)
      }

      parseInsn(s"invoke static $textified;") match {
        case insn: InvokeInsn => insn.getInvoke shouldEqual new StaticInvoke(methodRef, false)
      }
    }
  }

  it should "parse static/special invokes of methods in interfaces" in {
    forAll (methodRefs) { (textified, methodRef) =>
      parseInsn(s"invoke special interface $textified;") match {
        case insn: InvokeInsn => insn.getInvoke shouldEqual new SpecialInvoke(methodRef, true)
      }

      parseInsn(s"invoke static interface $textified;") match {
        case insn: InvokeInsn => insn.getInvoke shouldEqual new StaticInvoke(methodRef, true)
      }
    }
  }

  it should "parse invoke dynamic instructions" in {
    parseInsn(
      """invoke dynamic {
        |  name = "foo",
        |  type = (int, long) void,
        |  bootstrap = invoke static java.lang.Integer.parseInt(java.lang.String) : int,
        |  arguments = [ 10, "foo", java.lang.String.class ]
        |};
      """.stripMargin.trim) match {
      case insn: InvokeInsn =>
        val desc = new MethodDescriptor(List[Type](IntType.getInstance, LongType.getInstance).asJava, Optional.empty())
        val bootstrapMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance))
        val bootstrap = new InvokeStaticHandle(bootstrapMethod, false)
        val arguments = List[BootstrapConstant](new IntConstant(10), new StringConstant("foo"), new ClassConstant(new ObjectType(Path.STRING))).asJava
        insn.getInvoke shouldEqual new DynamicInvoke("foo", desc, bootstrap, arguments)
    }
  }

  it should "parse load instructions" in {
    val ctx = new UnresolvedStackCtx()
    val local = ctx.getLocal("local")

    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"load $typString local;", ctx) match {
        case insn: LoadInsn =>
          insn.getType shouldEqual typ
          insn.getLocal shouldEqual local
      }
    }
  }

  it should "parse store instructions" in {
    val ctx = new UnresolvedStackCtx()
    val local = ctx.getLocal("local")

    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"store $typString local;", ctx) match {
        case insn: StoreInsn =>
          insn.getType shouldEqual typ
          insn.getLocal shouldEqual local
      }
    }
  }

  it should "parse monitor enter instructions" in {
    assert(parseInsn(s"monitor enter;").isInstanceOf[MonitorEnterInsn])
  }

  it should "parse monitor exit instructions" in {
    assert(parseInsn(s"monitor exit;").isInstanceOf[MonitorExitInsn])
  }

  it should "parse neg insn" in {
    forAll (intLongFloatDouble) { (typString, typ) =>
      parseInsn(s"neg $typString;") match {
        case insn: NegInsn => insn.getType shouldEqual typ
      }
    }
  }

  it should "parse new array insns" in {
    parseInsn("new int[?];") match {
      case insn: NewArrayInsn =>
        insn.getType shouldEqual new ArrayType(IntType.getInstance, 1)
        insn.getInitializedDimensions shouldEqual 1
    }

    parseInsn("new int[?][];") match {
      case insn: NewArrayInsn =>
        insn.getType shouldEqual new ArrayType(IntType.getInstance, 2)
        insn.getInitializedDimensions shouldEqual 1
    }

    parseInsn("new int[?][?][?];") match {
      case insn: NewArrayInsn =>
        insn.getType shouldEqual new ArrayType(IntType.getInstance, 3)
        insn.getInitializedDimensions shouldEqual 3
    }
  }

  it should "parse 'new' instructions" in {
    parseInsn("new java.lang.String;") match {
      case insn: NewInsn => insn.getPath shouldEqual Path.STRING
    }
  }

  it should "parse primitive cast instructions" in {
    forAll (intLongFloatDouble) { (fromTypeString, fromType) =>
      forAll(primitiveType) { (toTypeString, toType) =>
        whenever(!toType.isInstanceOf[BooleanType]) {
          parseInsn(s"cast $fromTypeString -> $toTypeString;") match {
            case insn: PrimitiveCastInsn =>
              insn.getFromType shouldEqual fromType
              insn.getToType shouldEqual toType
          }
        }
      }
    }
  }

  it should "parse ref cast instructions" in {
    parseInsn(s"cast java.lang.String;") match {
      case insn: RefCastInsn => insn.getType shouldEqual new ObjectType(Path.STRING)
    }

    parseInsn(s"cast java.lang.String[];") match {
      case insn: RefCastInsn => insn.getType shouldEqual new ArrayType(new ObjectType(Path.STRING), 1)
    }
  }

  it should "parse push instructions" in {
    parseInsn("push 10;") match {
      case insn: PushInsn => insn.getConstant shouldEqual new IntConstant(10)
    }

    parseInsn("push 10L;") match {
      case insn: PushInsn => insn.getConstant shouldEqual new LongConstant(10)
    }

    parseInsn("push java.lang.String.class;") match {
      case insn: PushInsn => insn.getConstant shouldEqual new ClassConstant(new ObjectType(Path.STRING))
    }
  }

  it should "parse return instructions" in {
    parseInsn("return;") match {
      case insn: ReturnInsn => insn.getType shouldEqual Optional.empty
    }

    parseInsn("return int;") match {
      case insn: ReturnInsn => insn.getType shouldEqual Optional.of(IntType.getInstance)
    }

    parseInsn("return ref;") match {
      case insn: ReturnInsn => assert(insn.getType.get.isInstanceOf[RefType])
    }
  }

  it should "parse throw instruction" in {
    assert(parseInsn("throw;").isInstanceOf[ThrowInsn])
    assert(parseInsn("throw ;").isInstanceOf[ThrowInsn])
  }

  it should "parse swap instructions" in {
    assert(parseInsn("swap;").isInstanceOf[SwapInsn])
    assert(parseInsn("swap ;").isInstanceOf[SwapInsn])
  }

  it should "parse pop instructions" in {
    assert(parseInsn("pop;").isInstanceOf[PopInsn])
    assert(parseInsn("pop ;").isInstanceOf[PopInsn])
  }

  it should "parse all kinds of dup instructions" in {
    assert(parseInsn("dup;").isInstanceOf[DupInsn])
    assert(parseInsn("dup x1;").isInstanceOf[DupX1Insn])
    assert(parseInsn("dup x2;").isInstanceOf[DupX2Insn])
    assert(parseInsn("dup2;").isInstanceOf[Dup2Insn])
    assert(parseInsn("dup2 x1;").isInstanceOf[Dup2X1Insn])
    assert(parseInsn("dup2 x2;").isInstanceOf[Dup2X2Insn])
  }
}
