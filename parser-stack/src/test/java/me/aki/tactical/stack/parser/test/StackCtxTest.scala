package me.aki.tactical.stack.parser.test

import me.aki.tactical.core.util.Cell
import me.aki.tactical.stack.StackLocal
import me.aki.tactical.stack.insn.{Instruction, ReturnInsn}
import me.aki.tactical.stack.parser.StackCtx

trait StackCtxTest {
  val local1 = new StackLocal
  val local2 = new StackLocal
  val local3 = new StackLocal
  val local4 = new StackLocal

  def newCtx = new StackCtx(Map(
    "local1" -> local1,
    "local2" -> local2,
    "local3" -> local3,
    "local4" -> local4
  ))

  /**
    * Ensure that certain instruction references point to the correct labels
    *
    * @param ctx the context where the cells/label reference have been registered
    * @param cells map from the name of a label to all cells referencing it
    */
  def validateLabels(ctx: StackCtx, cells: Map[String, Cell[Instruction]]): Unit = {
    for ((label, cell) ← cells) {
      val dummyInsn = new ReturnInsn()
      cell.set(dummyInsn)

      val refs = ctx.getUnresolvedReferences.getOrElse(label, Set())
      refs.foreach(_ set dummyInsn)
      assert(refs.exists(_.get eq dummyInsn))
    }
  }
}
