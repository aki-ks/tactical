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

  /** Label references that were add before the label was known to this context */
  private val unresolvedLabelReferences = mutable.Map[String, Set[Cell[Instruction]]]()

  /**
    * Register a new reference of a label.
    * It gets resolved as soon as the label is registered.
    *
    * @param label name of the label
    * @param cell cell containing a the usage of the label
    */
  def registerLabelReference(label: String, cell: Cell[Instruction]) = {
    val unresolvedCells = unresolvedLabelReferences.getOrElse(label, Set())
    unresolvedLabelReferences(label) = unresolvedCells + cell
  }

  /**
    * Register a new label.
    *
    * @param label name of the label
    * @param target instruction that this label points to
    */
  def registerLabel(label: String, target: Instruction) = {
    for {
      cells ← unresolvedLabelReferences.remove(label)
      cell ← cells
    } cell.set(target)
  }

  /**
    * Get a map from references to labels not yet registered labels.
    *
    * @return map of labels names to references of that label
    */
  def getUnresolvedReferences: Map[String, Set[Cell[Instruction]]] =
    this.unresolvedLabelReferences.toMap

  def resolve(labels: Map[String, Instruction]): ResolvedStackCtx = {
    for {
      (labelName, cells) ← unresolvedLabelReferences
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
