package me.aki.tactical.stack.parser

import scala.collection.JavaConverters._
import java.util.{Optional, LinkedHashMap => JLinkedHashMap}

import fastparse.all._
import me.aki.tactical.core.`type`.ArrayType
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.core.parser.InsnTypeParser._
import me.aki.tactical.stack.insn._
import me.aki.tactical.stack.invoke._

class InsnParser(ctx: StackCtx) extends Parser[Instruction] {
  val parser: P[Instruction] = P {
    new MathInsnParser(ctx) |
      new GotoInsnParser(ctx) |
      new IfInsnParser(ctx) |
      new SwitchInsnParser(ctx) |
      new FieldInsnParser(ctx) |
      ArrayLengthParser |
      ArrayLoadInsnParser |
      ArrayStoreInsnParser |
      new IncrementInsnParser(ctx) |
      InstanceOfParser |
      InvokeInsnParser |
      new LoadInsnParser(ctx) |
      new StoreInsnParser(ctx) |
      MonitorEnterInsnParser |
      MonitorExitInsnParser |
      NegInsnParser |
      NewArrayInsnParser |
      NewInsnParser |
      PrimitiveCastInsnParser |
      RefCastInsnParser |
      PushInsnParser |
      ReturnInsnParser |
      ThrowInsnParser |
      SwapInsnParser |
      PopInsnParser |
      DupInsnParser
  }
}

class MathInsnParser(ctx: StackCtx) extends Parser[AbstractBinaryMathInsn] {
  val parser: P[AbstractBinaryMathInsn] = P {
    P { for (typ ← "add" ~ WS ~ ilfd ~ WS.? ~ ";") yield new AddInsn(typ) } |
      P { for (typ ← "sub" ~ WS ~ ilfd ~ WS.? ~ ";") yield new SubInsn(typ) } |
      P { for (typ ← "mul" ~ WS ~ ilfd ~ WS.? ~ ";") yield new MulInsn(typ) } |
      P { for (typ ← "div" ~ WS ~ ilfd ~ WS.? ~ ";") yield new DivInsn(typ) } |
      P { for (typ ← "mod" ~ WS ~ ilfd ~ WS.? ~ ";") yield new ModInsn(typ) } |
      P { for (typ ← "and" ~ WS ~ il ~ WS.? ~ ";") yield new AndInsn(typ) } |
      P { for (typ ← "or" ~ WS ~ il ~ WS.? ~ ";") yield new OrInsn(typ) } |
      P { for (typ ← "xor" ~ WS ~ il ~ WS.? ~ ";") yield new XorInsn(typ) } |
      P { for (typ ← "shl" ~ WS ~ il ~ WS.? ~ ";") yield new ShlInsn(typ) } |
      P { for (typ ← "shr" ~ WS ~ il ~ WS.? ~ ";") yield new ShrInsn(typ) } |
      P { for (typ ← "ushr" ~ WS ~ il ~ WS.? ~ ";") yield new UShrInsn(typ) } |
      P { for (typ ← "cmp" ~ WS.? ~ ";") yield new CmpInsn() } |
      P { for (typ ← "cmpl" ~ WS ~ fd ~ WS.? ~ ";") yield new CmplInsn(typ) } |
      P { for (typ ← "cmpg" ~ WS ~ fd ~ WS.? ~ ";") yield new CmpgInsn(typ) }
  }
}

class GotoInsnParser(ctx: StackCtx) extends Parser[GotoInsn] {
  val parser: P[GotoInsn] =
    for (label ← "goto" ~ WS ~ Literal ~ WS.? ~ ";") yield {
      val insn = new GotoInsn(null)
      ctx.addLabelReference(label, insn.getTargetCell)
      insn
    }
}

class IfInsnParser(ctx: StackCtx) extends Parser[IfInsn] {
  import IfInsn._

  val conditionParser = P {
    val intCondition = P {
      val intComparison =
        P { for (_ ← "==".!) yield EQ.getInstance } |
          P { for (_ ← "!=".!) yield NE.getInstance } |
          P { for (_ ← "<=".!) yield LE.getInstance } |
          P { for (_ ← "<".!) yield LT.getInstance } |
          P { for (_ ← ">=".!) yield GE.getInstance } |
          P { for (_ ← ">".!) yield GT.getInstance }

      val intCompareValue =
        P { for (_ ← "?".!) yield StackValue.getInstance } |
          P { for (_ ← "0".!) yield ZeroValue.getInstance }

      for ((comparison, compareValue) <- "?" ~ WS.? ~ intComparison ~ WS.? ~ intCompareValue)
        yield new IntCondition(comparison, compareValue)
    }
    val refCondition = P {
      val refComparison =
        P { for (_ ← "==".!) yield EQ.getInstance } |
          P { for (_ ← "!=".!) yield NE.getInstance }

      val refCompareValue =
        P { for (_ ← "?".!) yield StackValue.getInstance } |
          P { for (_ ← "null".!) yield NullValue.getInstance }

      for ((comparison, compareValue) <- "?" ~ WS.? ~ refComparison ~ WS.? ~ refCompareValue)
        yield new ReferenceCondition(comparison, compareValue)
    }

    P { "int" ~ WS.? ~ "(" ~ WS.? ~ intCondition ~ WS.? ~ ")" } |
    P { "ref" ~ WS.? ~ "(" ~ WS.? ~ refCondition ~ WS.? ~ ")" }
  }

  val parser: P[IfInsn] =
    for((condition, label) ← "if" ~ WS ~ conditionParser ~ WS.? ~ "goto" ~ WS ~ Literal ~ WS.? ~ ";") yield {
      val insn = new IfInsn(condition, null)
      ctx.addLabelReference(label, insn.getTargetCell)
      insn
    }
}

class SwitchInsnParser(ctx: StackCtx) extends Parser[SwitchInsn] {
  val parser: P[SwitchInsn] = P {
    val cases = "case" ~ WS.? ~ int ~ WS.? ~ ":" ~ WS.? ~ "goto" ~ WS.? ~ Literal~ WS.? ~ ";"
    val default = "default" ~ WS.? ~ ":" ~ WS.? ~ "goto" ~ WS.? ~ Literal ~ WS.? ~ ";"

    val switchParser = P {
      "switch" ~ WS.? ~ "{" ~ WS.? ~
        cases.rep(sep = WS.?) ~ WS.? ~
        default ~ WS.? ~
      "}"
    }

    for ((cases, default) ← switchParser) yield {
      val branchTable = new JLinkedHashMap[Integer, Instruction]()
      for ((key, _) ← cases) branchTable.put(key, null)
      val insn = new SwitchInsn(branchTable, null)

      for ((key, label) ← cases) ctx.addLabelReference(label, insn.getBranchTableCell(key))
      ctx.addLabelReference(default, insn.getDefaultLocationCell)

      insn
    }
  }
}

class FieldInsnParser(ctx: StackCtx) extends Parser[AbstractFieldInsn] {
  val parser: P[AbstractFieldInsn] = P {
    val static: P[Boolean] = for (opt ← ("static".! ~ WS).?) yield opt.isDefined

    P { for ((static, field) ← "get" ~ WS ~ static ~ FieldRefParser ~ ";") yield new FieldGetInsn(field, static) } |
      P { for ((static, field) ← "set" ~ WS ~ static ~ FieldRefParser ~ ";") yield new FieldSetInsn(field, static) }
  }
}

object ArrayLengthParser extends Parser[ArrayLengthInsn] {
  val parser: P[ArrayLengthInsn] =
    for (_ ← "arraylength" ~ WS.? ~ ";") yield new ArrayLengthInsn()
}

object ArrayLoadInsnParser extends Parser[ArrayLoadInsn] {
  val parser: P[ArrayLoadInsn] =
    for (typ ← "arrayload" ~ WS ~ ilfdref ~ WS.? ~ ";")
      yield new ArrayLoadInsn(typ)
}

object ArrayStoreInsnParser extends Parser[ArrayStoreInsn] {
  val parser: P[ArrayStoreInsn] =
    for (typ ← "arraystore" ~ WS ~ ilfdref ~ WS.? ~ ";")
      yield new ArrayStoreInsn(typ)
}

class IncrementInsnParser(ctx: StackCtx) extends Parser[IncrementInsn] {
  val parser: P[IncrementInsn] =
    for ((local, number) ← "inc" ~ WS ~ Literal ~ WS ~ int ~ WS.? ~ ";")
      yield new IncrementInsn(ctx.getLocal(local), number)
}

object InstanceOfParser extends Parser[InstanceOfInsn] {
  val parser: P[InstanceOfInsn] =
    for (typ ← "instanceof" ~ WS ~ RefTypeParser ~ ";") yield new InstanceOfInsn(typ)
}

object InvokeInsnParser extends Parser[InvokeInsn] {
  val parser: P[InvokeInsn] = P {
    val dynamicInvokeParser = P[DynamicInvoke] {
      val arguments = "[" ~ WS.? ~ BootstrapConstantParser.rep(sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "]"

      for {
        (name, typ, bootstrap, arguments) ←
          "dynamic" ~ WS.? ~ "{" ~ WS.? ~
            "name" ~ WS.? ~ "=" ~ WS.? ~ StringLiteral ~ WS.? ~ "," ~ WS.? ~
            "type" ~ WS.? ~ "=" ~ WS.? ~ MethodDescriptorParser ~ WS.? ~ "," ~ WS.? ~
            "bootstrap" ~ WS.? ~ "=" ~ WS.? ~ HandleParser ~ WS.? ~ "," ~ WS.? ~
            "arguments" ~ WS.? ~ "=" ~ WS.? ~ arguments ~ WS.? ~
          "}"
      } yield new DynamicInvoke(name, typ, bootstrap, arguments.asJava)
    }

    val invokeParser: P[Invoke] = {
      val interface: P[Boolean] = ("interface".! ~ WS).?.map(_.isDefined)

      dynamicInvokeParser |
        P { for (method ← "interface" ~ WS ~ MethodRefParser) yield new InterfaceInvoke(method) } |
        P { for (method ← "virtual" ~ WS ~ MethodRefParser) yield new VirtualInvoke(method) } |
        P { for ((interface, method) ← "static" ~ WS ~ interface ~ MethodRefParser) yield new StaticInvoke(method, interface) } |
        P { for ((interface, method) ← "special" ~ WS ~ interface ~ MethodRefParser) yield new SpecialInvoke(method, interface)}
    }

    for (invoke ← "invoke" ~ WS ~ invokeParser ~ WS.? ~ ";") yield new InvokeInsn(invoke)
  }
}

class LoadInsnParser(ctx: StackCtx) extends Parser[LoadInsn] {
  val parser: P[LoadInsn] =
    for ((typ, local) ← "load" ~ WS ~ ilfdref ~ WS ~ Literal ~ WS.? ~ ";")
      yield new LoadInsn(typ, ctx.getLocal(local))
}

class StoreInsnParser(ctx: StackCtx) extends Parser[StoreInsn] {
  val parser: P[StoreInsn] =
    for ((typ, local) ← "store" ~ WS ~ ilfdref ~ WS ~ Literal ~ WS.? ~ ";")
      yield new StoreInsn(typ, ctx.getLocal(local))
}

object MonitorEnterInsnParser extends Parser[MonitorEnterInsn] {
  val parser: P[MonitorEnterInsn] =
    for (_ ← "monitor" ~ WS.? ~ "enter" ~ WS.? ~ ";") yield new MonitorEnterInsn()
}

object MonitorExitInsnParser extends Parser[MonitorExitInsn] {
  val parser: P[MonitorExitInsn] =
    for (_ ← "monitor" ~ WS.? ~ "exit" ~ WS.? ~ ";") yield new MonitorExitInsn()
}

object NegInsnParser extends Parser[NegInsn] {
  val parser: P[NegInsn] =
    for (typ ← "neg" ~ WS.? ~ ilfd ~ WS.? ~ ";") yield new NegInsn(typ)
}

object NewArrayInsnParser extends Parser[NewArrayInsn] {
  val parser: P[NewArrayInsn] = P {
    val sizedDimensions = P { "[" ~ WS.? ~ "?" ~ WS.? ~ "]" }.!.rep(min = 1, sep = WS.?)
    val unsizedDimensions = P { "[" ~ WS.? ~ "]" }.!.rep(min = 0, sep = WS.?)

    for ((typ, sizedDimensions, unsizedDimensions) ← "new" ~ WS ~ ArrayBaseTypeParser ~ sizedDimensions ~ WS.? ~ unsizedDimensions ~ WS.? ~ ";") yield {
      val arrayType = new ArrayType(typ, sizedDimensions.size + unsizedDimensions.size)
      new NewArrayInsn(arrayType, sizedDimensions.size)
    }
  }
}

object NewInsnParser extends Parser[NewInsn] {
  val parser: P[NewInsn] =
    for (path ← "new" ~ WS ~ PathParser ~ WS.? ~ ";") yield new NewInsn(path)
}

object PrimitiveCastInsnParser extends Parser[PrimitiveCastInsn] {
  val parser: P[PrimitiveCastInsn] =
    for ((from, to) ← "cast" ~ WS.? ~ ilfd ~ WS.? ~ ("->" | "→") ~ WS.? ~ bscilfd ~ WS.? ~ ";")
      yield new PrimitiveCastInsn(from, to)
}

object RefCastInsnParser extends Parser[RefCastInsn] {
  val parser: P[RefCastInsn] =
    for (typ ← "cast" ~ WS.? ~ RefTypeParser ~ WS.? ~ ";") yield new RefCastInsn(typ)
}

object PushInsnParser extends Parser[PushInsn] {
  val parser: P[PushInsn] =
    for (constant ← "push" ~ WS ~ PushableConstantParser ~ WS.? ~ ";") yield new PushInsn(constant)
}

object ReturnInsnParser extends Parser[ReturnInsn] {
  val parser: P[ReturnInsn] =
    for (typ ← "return" ~ (WS ~ ilfdref).? ~ WS.? ~ ";") yield new ReturnInsn(Optional.ofNullable(typ.orNull))
}

object ThrowInsnParser extends Parser[ThrowInsn] {
  val parser: P[ThrowInsn] =
    for (_ ← "throw" ~ WS.? ~ ";") yield new ThrowInsn()
}

object SwapInsnParser extends Parser[SwapInsn] {
  val parser: P[SwapInsn] =
    for (_ ← "swap" ~ WS.? ~ ";") yield new SwapInsn
}

object PopInsnParser extends Parser[PopInsn] {
  val parser: P[PopInsn] =
    for (_ ← "pop" ~ WS.? ~ ";") yield new PopInsn
}

object DupInsnParser extends Parser[Instruction] {
  val parser: P[Instruction] = P {
    "dup" ~ P {
      ("2" ~ WS ~ "x1").!.map(_ => new Dup2X1Insn) |
        ("2" ~ WS ~ "x2").!.map(_ => new Dup2X2Insn) |
        "2".!.map(_ => new Dup2Insn) |
        (WS ~ "x1").!.map(_ => new DupX1Insn) |
        (WS ~ "x2").!.map(_ => new DupX2Insn) |
        Pass.!.map(_ => new DupInsn)
    } ~ WS.? ~ ";"
  }
}