package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._

import me.aki.tactical.ref.RefLocal
import me.aki.tactical.ref.parser.{ExpressionParser, UnresolvedRefCtx}
import me.aki.tactical.ref.textifier.TextifyCtx

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class AbstractUnresolvedCtxTest extends FlatSpec with Matchers with PropertyChecks {
  val local1 = new RefLocal(null)
  val local2 = new RefLocal(null)
  val local3 = new RefLocal(null)

  val locals = Map(
    "local1" -> local1,
    "local2" -> local2,
    "local3" -> local3
  )

  val parseCtx = new UnresolvedRefCtx(locals)
  val expr = new ExpressionParser(parseCtx)

  val textifyCtx = new TextifyCtx(locals.map(_.swap).asJava, Map().asJava)
}
