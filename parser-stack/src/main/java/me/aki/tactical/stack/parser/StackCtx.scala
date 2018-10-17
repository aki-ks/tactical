package me.aki.tactical.stack.parser

import scala.collection.mutable

import me.aki.tactical.core.util.Cell
import me.aki.tactical.stack.StackLocal
import me.aki.tactical.stack.insn.Instruction

abstract class StackCtx {
  var locals: Map[String, StackLocal]

  /**
    * Get a Local by its name or create a new one.
    *
    * @param name name of the local
    * @return the resolved local
    */
  def getLocal(name: String) =
    locals get name match {
      case Some(local) => local
      case None =>
        val local = new StackLocal
        locals += name -> local
        local
    }
}

/** A context that's still resolving labels */
class UnresolvedStackCtx() extends StackCtx {
  var locals = Map[String, StackLocal]()

  private var labelReferences = Map[String, Set[Cell[Instruction]]]()

  /**
    * Register a new reference of a label.
    *
    * @param label name of the label
    * @param cell cell containing a the usage of the label
    */
  def registerLabelReference(label: String, cell: Cell[Instruction]) = {
    val otherCells = labelReferences.getOrElse(label, Set())
    labelReferences += label -> (otherCells + cell)
  }


  def resolve(labels: Map[String, Instruction]): ResolvedStackCtx = {
    for {
      (labelName, cells) ← labelReferences
      val label = labels.getOrElse(labelName, throw new NoSuchElementException(s"No such label '$labelName'"))
      cell ← cells
    } cell.set(label)

    new ResolvedStackCtx(locals, labels.toMap)
  }
}

/** A context where all labels have already been resolved. */
class ResolvedStackCtx(var locals: Map[String, StackLocal], labels: Map[String, Instruction]) extends StackCtx {
  def getLabel(name: String) =
    labels.getOrElse(name, throw new NoSuchElementException("No such label"))
}
