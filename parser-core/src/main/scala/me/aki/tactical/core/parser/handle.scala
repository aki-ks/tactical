package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.{FieldRef, MethodRef, Path}
import me.aki.tactical.core.handle._

object FieldRefParser extends Parser[FieldRef] {
  val pathAndName = for (literals ← Literal.rep(min = 2, sep = WS.? ~ "." ~ WS.?)) yield {
    val name = literals.last
    val path = literals.init
    (new Path(path.init.toArray, path.last), name)
  }

  val parser: P[FieldRef] = P {
    for ((owner, name, typ) ← pathAndName ~ WS.? ~ ":" ~ WS.? ~ TypeParser ~ WS.?)
      yield new FieldRef(owner, name, typ)
  }
}

object MethodRefParser extends Parser[MethodRef] {
  val parser: P[MethodRef] = P {
    val parameters = TypeParser.rep(sep = WS.? ~ "," ~ WS.?)

    for ((owner, name, params, returnType) ← FieldRefParser.pathAndName ~ WS.? ~ "(" ~ WS.? ~ parameters ~ WS.? ~ ")" ~ WS.? ~ ":" ~ WS.? ~ ReturnTypeParser)
      yield new MethodRef(owner, name, params.asJava, returnType)
  }
}

object FieldHandleParser extends Parser[FieldHandle] {
  val parser: P[FieldHandle] =
    P { for (field ← "get" ~ WS ~ "static" ~ WS ~ FieldRefParser) yield new GetStaticHandle(field) } |
      P { for (field ← "get" ~ WS ~ FieldRefParser) yield new GetFieldHandle(field) } |
      P { for (field ← "set" ~ WS ~ "static" ~ WS ~ FieldRefParser) yield new SetStaticHandle(field) } |
      P { for (field ← "set" ~ WS ~ FieldRefParser) yield new SetFieldHandle(field) }
}

object MethodHandleParser extends Parser[MethodHandle] {
  val parser: P[MethodHandle] = P {
    "invoke" ~ WS ~ P {
      val interface = P { "interface".! ~ WS }.?.map(_.isDefined)

      P { for (method ← "interface" ~ WS ~ MethodRefParser) yield new InvokeInterfaceHandle(method) } |
        P { for ((iface, method) ← "special" ~ WS ~ interface ~ MethodRefParser) yield new InvokeSpecialHandle(method, iface) } |
        P { for ((iface, method) ← "static" ~ WS ~ interface ~ MethodRefParser) yield new InvokeStaticHandle(method, iface) } |
        P { for (method ← "virtual" ~ WS ~ MethodRefParser) yield new InvokeVirtualHandle(method) } |
        P { for (method ← "new" ~ WS ~ MethodRefParser) yield new NewInstanceHandle(method) }
    }
  }
}

object HandleParser extends Parser[Handle] {
  val parser: P[Handle] = FieldHandleParser | MethodHandleParser
}
