package me.aki.tactical.stack.parser

import scala.collection.mutable

import me.aki.tactical.core.util.Cell
import me.aki.tactical.stack.StackLocal
import me.aki.tactical.stack.insn.Instruction

class StackCtx(locals: Map[String, StackLocal]) {
  /** Map labels to the instructions they points to */
  private val labels = mutable.Map[String, Instruction]()

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
    labels get label match {
      case Some(label) => cell.set(label)
      case None =>
        val unresolvedCells = unresolvedLabelReferences.getOrElse(label, Set())
        unresolvedLabelReferences(label) = unresolvedCells + cell
    }
  }

  /**
    * Add a new label.
    *
    * @param label name of the label
    * @param target instruction that this label points to
    */
  def registerLabel(label: String, target: Instruction) = {
    labels(label) = target

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

  /**
    * Get a Local by its name
    *
    * @param name name of the local
    * @return the resolved local
    */
  def getLocal(name: String): StackLocal =
    locals.getOrElse(name, throw new RuntimeException("No such local"))
}
