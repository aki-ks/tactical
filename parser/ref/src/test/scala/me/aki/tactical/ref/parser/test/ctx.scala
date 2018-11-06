package me.aki.tactical.ref.parser.test

import me.aki.tactical.core.constant.NullConstant

import scala.collection.JavaConverters._
import me.aki.tactical.ref.RefLocal
import me.aki.tactical.ref.expr.ConstantExpr
import me.aki.tactical.ref.parser.{ResolvedRefCtx, UnresolvedRefCtx}
import me.aki.tactical.ref.stmt.{GotoStmt, ReturnStmt, ThrowStmt}
import me.aki.tactical.ref.textifier.TextifyCtx
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

abstract class AbstractCtxTest extends FlatSpec with Matchers with PropertyChecks {
  val local1 = new RefLocal(null)
  val local2 = new RefLocal(null)
  val local3 = new RefLocal(null)

  val label1 = new ReturnStmt()
  val label2 = new ThrowStmt(new ConstantExpr(NullConstant.getInstance))
  val label3 = new GotoStmt(label2)

  val locals = Map(
    "local1" → local1,
    "local2" → local2,
    "local3" → local3
  )

  val labels = Map(
    "label1" → label1,
    "label2" → label2,
    "label3" → label3
  )

  def textifyCtx = new TextifyCtx(locals.map(_.swap).asJava, labels.map(_.swap).asJava)
}

abstract class AbstractResolvedCtxTest extends AbstractCtxTest {
  def parseCtx = new ResolvedRefCtx(locals, labels)
}

abstract class AbstractUnresolvedCtxTest extends AbstractCtxTest {
  def parseCtx = new UnresolvedRefCtx(locals)
}
