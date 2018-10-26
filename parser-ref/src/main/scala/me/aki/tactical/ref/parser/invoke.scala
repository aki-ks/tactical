package me.aki.tactical.ref.parser

import scala.collection.JavaConverters._
import java.util.{List => JList}
import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.core.`type`.Type
import me.aki.tactical.ref.Expression
import me.aki.tactical.ref.invoke._

class DynamicInvokeParser(ctx: UnresolvedRefCtx) extends Parser[InvokeDynamic] {
  val parser: P[InvokeDynamic] = {
    val expr: P[Expression] = new ExpressionParser(ctx)

    val methodTypeParser: P[(MethodDescriptor, JList[Expression])] = P {
      val parameters = (expr ~ WS.? ~ ":" ~ WS.? ~ TypeParser).rep(sep = WS.? ~ "," ~ WS.?)
      "(" ~ WS.? ~ parameters ~ WS.? ~ ")" ~ WS.? ~ ":" ~ WS.? ~ ReturnTypeParser
    } map {
      case (params, returnType) =>
        val parameterTypes = params.map(_._2).asJava
        val parameters = params.map(_._1).asJava
        (new MethodDescriptor(parameterTypes, returnType), parameters)
    }

    for {
      (name, (descriptor, arguments), bootstrapMethod, bootstrapArguments) ←
        "invoke" ~ WS ~ "dynamic" ~ WS.? ~ "{" ~ WS.? ~
          "name" ~ WS.? ~ "=" ~ WS.? ~ StringLiteral ~ WS.? ~
          "," ~ WS.? ~ "type" ~ WS.? ~ "=" ~ WS.? ~ methodTypeParser ~ WS.? ~
          "," ~ WS.? ~ "bootstrap" ~ WS.? ~ "=" ~ WS.? ~ HandleParser ~ WS.? ~
          ("," ~ WS.? ~ "arguments" ~ WS.? ~ "=" ~ WS.? ~ "[" ~ WS.? ~ BootstrapConstantParser.rep(sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "]" ~ WS.?).? ~
          "}"
    } yield new InvokeDynamic(name, descriptor, bootstrapMethod, bootstrapArguments.getOrElse(Nil).asJava, arguments)
  }
}

class ConcreteInvokeParser(ctx: UnresolvedRefCtx) extends Parser[AbstractConcreteInvoke] {
  val parser: P[AbstractConcreteInvoke] = {
    val expr: P[Expression] = new ExpressionParser(ctx)

    val typeAndInstance = P[(MethodRef, JList[Expression]) => AbstractConcreteInvoke] {
      val iface = P { "interface".!.? } map (_.isDefined)

      P { for (instance ← "interface" ~ WS.? ~ expr) yield (method: MethodRef, arguments: JList[Expression]) => new InvokeInterface(method, instance, arguments) } |
        P { for (instance ← "virtual" ~ WS.? ~ expr) yield (method: MethodRef, arguments: JList[Expression]) => new InvokeVirtual(method, instance, arguments) } |
        P { for ((interface, instance) ← "special" ~ WS.? ~ iface ~ WS.? ~ expr) yield (method: MethodRef, arguments: JList[Expression]) => new InvokeSpecial(method, instance, arguments, interface) } |
        P { for (interface ← "static" ~ WS.? ~ iface) yield (method: MethodRef, arguments: JList[Expression]) => new InvokeStatic(method, arguments, interface) }
    }

    val paramList = P {
      val paramTypes = P { expr ~ WS.? ~ ":" ~ WS.? ~ TypeParser }.rep(sep = WS.? ~ "," ~ WS.?)
      "(" ~ WS.? ~ paramTypes ~ WS.? ~ ")"
    }

    for {
      (path, apply, name, params, returnType) ←
        PathParser ~ WS.? ~ "." ~ WS.? ~ "<" ~ WS.? ~ typeAndInstance ~ WS.? ~ ">" ~ WS.? ~ "." ~ Literal ~ WS.? ~ paramList ~ WS.? ~ ":" ~ WS.? ~ ReturnTypeParser
    } yield {
      val methodRef = {
        val argumentTypes: JList[Type] = params.map(_._2).asJava
        new MethodRef(path, name, argumentTypes, returnType)
      }
      val parameters: JList[Expression] = params.map(_._1).asJava
      apply(methodRef, parameters)
    }
  }
}

class AbstractInvokeParser(ctx: UnresolvedRefCtx) extends Parser[AbstractInvoke] {
  val parser: P[AbstractInvoke] = new ConcreteInvokeParser(ctx) | new DynamicInvokeParser(ctx)
}
