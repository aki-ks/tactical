package me.aki.tactical.ref.parser

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.{FieldRef, Path}
import me.aki.tactical.core.`type`.ArrayType
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.{Expression, RefLocal, Variable}
import me.aki.tactical.ref.expr._

class VariableParser(ctx: UnresolvedRefCtx) extends Parser[Variable] {
  val arrayBoxParser =
    for (expr ← new ExpressionParser(ctx) if expr.isInstanceOf[ArrayBoxExpr])
      yield expr.asInstanceOf[ArrayBoxExpr]

  val parser: P[Variable] = P {
    StaticFieldExprParser |
    new InstanceFieldExprParser(ctx) |
    arrayBoxParser |
    new LocalParser(ctx)
  }
}

class ExpressionParser(ctx: UnresolvedRefCtx) extends Parser[Expression] {
  // These parsers do not start with another expression.
  // The separation is necessary to prevent a stackoverflow.
  val nonPrefixed: P[Expression] = P {
    ConstantExpressionParser |
    new NegExpressionParser(ctx) |
    new NewArrayParser(ctx) |
    NewExpressionParser |
    new CastParser(ctx) |
    new InvokeExprParser(ctx) |
    StaticFieldExprParser |
    new InstanceFieldExprParser(ctx) |
    new LocalParser(ctx) |
    P { "(" ~ WS.? ~ parser ~ WS.? ~ ")" }
  }

  val parser: P[Expression] = nonPrefixed ~ WS.? flatMap { expr =>
    P { for (op2 ← "+" ~ WS.? ~ parser) yield new AddExpr(expr, op2) } |
      P { for (op2 ← "-" ~ WS.? ~ parser) yield new SubExpr(expr, op2) } |
      P { for (op2 ← "*" ~ WS.? ~ parser) yield new MulExpr(expr, op2) } |
      P { for (op2 ← "/" ~ WS.? ~ parser) yield new DivExpr(expr, op2) } |
      P { for (op2 ← "%" ~ WS.? ~ parser) yield new ModExpr(expr, op2) } |
      P { for (op2 ← "&" ~ WS.? ~ parser) yield new AndExpr(expr, op2) } |
      P { for (op2 ← "|" ~ WS.? ~ parser) yield new OrExpr(expr, op2) } |
      P { for (op2 ← "^" ~ WS.? ~ parser) yield new XorExpr(expr, op2) } |
      P { for (op2 ← "cmpl" ~ WS.? ~ parser) yield new CmplExpr(expr, op2) } |
      P { for (op2 ← "cmpg" ~ WS.? ~ parser) yield new CmpgExpr(expr, op2) } |
      P { for (op2 ← "cmp" ~ WS.? ~ parser) yield new CmpExpr(expr, op2) } |
      P { for (op2 ← "<<" ~ WS.? ~ parser) yield new ShlExpr(expr, op2) } |
      P { for (op2 ← ">>>" ~ WS.? ~ parser) yield new UShrExpr(expr, op2) } |
      P { for (op2 ← ">>" ~ WS.? ~ parser) yield new ShrExpr(expr, op2) } |
      P { for (_ ← "." ~ WS.? ~ "length") yield new ArrayLengthExpr(expr) } |
      P { for (typ ← "instanceof" ~ WS.? ~ RefTypeParser) yield new InstanceOfExpr(typ, expr) } |
      P { for (index ← "[" ~ WS.? ~ parser ~ WS.? ~ "]") yield new ArrayBoxExpr(expr, index) } |
      PassWith(expr)
  }
}

object ConstantExpressionParser extends Parser[ConstantExpr] {
  val parser: P[ConstantExpr] =
    for (const ← PushableConstantParser) yield new ConstantExpr(const)
}

class NegExpressionParser(ctx: UnresolvedRefCtx) extends Parser[NegExpr] {
  val parser: P[NegExpr] =
    for (value ← "-" ~ WS.? ~ new ExpressionParser(ctx)) yield new NegExpr(value)
}

object NewExpressionParser extends Parser[NewExpr] {
  val parser: P[NewExpr] =
    for (path ← "new" ~ WS.? ~ PathParser) yield new NewExpr(path)
}

class NewArrayParser(ctx: UnresolvedRefCtx) extends Parser[NewArrayExpr] {
  val parser: P[NewArrayExpr] = P {
    val definedDimensions = P { "[" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ "]" }.rep(min = 1, sep = WS.?)
    val undefinedDimensions = P { "[" ~ WS.? ~ "]" }.!.rep(sep = WS.?)
    "new" ~ WS.? ~ ArrayBaseTypeParser ~ WS.? ~ definedDimensions ~ WS.? ~ undefinedDimensions
  } map {
    case (baseType, definedDimensions, undefined) =>
      val arrayType = new ArrayType(baseType, definedDimensions.size + undefined.size)
      new NewArrayExpr(arrayType, definedDimensions.asJava)
  }
}

class CastParser(ctx: UnresolvedRefCtx) extends Parser[CastExpr] {
  val parser: P[CastExpr] =
    for ((typ, expr) ← "(" ~ WS.? ~ TypeParser ~ WS.? ~ ")" ~ WS.? ~ new ExpressionParser(ctx))
      yield new CastExpr(typ, expr)
}

class InvokeExprParser(ctx: UnresolvedRefCtx) extends Parser[InvokeExpr] {
  val parser: P[InvokeExpr] =
    for (invoke ← new AbstractInvokeParser(ctx)) yield new InvokeExpr(invoke)
}

object StaticFieldExprParser extends Parser[StaticFieldExpr] {
  val parser: P[StaticFieldExpr] = P {
    val nameAndOwner: P[(Path, String)] =
      for (literals ← Literal.rep(min = 2, sep = WS.? ~ "." ~ WS.?))
        yield (Path.of(literals.init : _*), literals.last)

    for ((owner, name, typ) ← nameAndOwner ~ WS.? ~ ":" ~ WS.? ~ TypeParser)
      yield new StaticFieldExpr(new FieldRef(owner, name, typ))
  }
}

class InstanceFieldExprParser(ctx: UnresolvedRefCtx) extends Parser[InstanceFieldExpr] {
  val parser: P[InstanceFieldExpr] =
    for ((owner, instance, name, typ) ← PathParser ~ WS.? ~ "." ~ WS.? ~ "<" ~ WS.? ~ new ExpressionParser(ctx) ~ WS.? ~ ">" ~ WS.? ~ "." ~ Literal ~ WS.? ~ ":" ~ WS.? ~ TypeParser)
      yield new InstanceFieldExpr(new FieldRef(owner, name, typ), instance)
}
