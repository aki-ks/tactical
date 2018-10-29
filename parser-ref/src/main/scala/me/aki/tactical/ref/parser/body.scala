package me.aki.tactical.ref.parser

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.`type`.{ObjectType, Type}
import me.aki.tactical.core.{Body, Classfile, Method}
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.RefLocal

case class RefArgumentCtx(params: Seq[(Type, String)])

class RefBodyParser extends BodyParser {
  type Ctx = RefArgumentCtx

  override def staticInitializerCtx = RefArgumentCtx(Nil)

  /** A parser for the parameter list of a method */
  override def parameterParser: P[(Ctx, List[Type])] = P {
    val params = TypeParser ~ WS ~ Literal
    for (params ← params.rep(sep = WS.? ~ "," ~ WS.?)) yield {
      val paramTypes = for ((param, _) ← params) yield param
      (RefArgumentCtx(params.toList), paramTypes.toList)
    }
  }

  override def bodyParser(classfile: Classfile, method: Method, ctx: Ctx): P[Body] = P {
    Fail
  }
}
