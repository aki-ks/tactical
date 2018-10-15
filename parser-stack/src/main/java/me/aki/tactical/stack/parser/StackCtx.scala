package me.aki.tactical.stack.parser

import me.aki.tactical.core.util.Cell
import me.aki.tactical.stack.StackLocal
import me.aki.tactical.stack.insn.Instruction

import scala.collection.mutable

class StackCtx(locals: Map[String, StackLocal]) {
  /** Map labels to the instructions they points to */
  private val labels = mutable.Map[String, Instruction]()

  private val labelReferences = mutable.Map[String, mutable.Set[Cell[Instruction]]]()

  /**
    * Register a new reference of a label. It gets resolved as soon as the label is visited
    *
    * @param label name of the label
    * @param cell cell containing a the usage of the label
    */
  def addLabelReference(label: String, cell: Cell[Instruction]) = {
    labelReferences.getOrElseUpdate(label, mutable.Set()) += cell

    for (target ← labels.get(label)) cell.set(target)
  }

  /**
    * Add a new label.
    *
    * @param label name of the label
    * @param target instruction that this label points to.
    */
  def addLabel(label: String, target: Instruction) = {
    labels(label) = target

    for {
      cells ← labelReferences.get(label)
      cell ← cells
    } cell.set(target)
  }

  /**
    * Get all cells that reference a certain label.
    *
    * @param label name of the label
    * @return all cells to the label
    */
  def getReferences(label: String) = labelReferences.getOrElse(label, Set())

  /**
    * Get a Local by its name
    *
    * @param name name of the local
    * @return the resolved local
    */
  def getLocal(name: String): StackLocal =
    locals.getOrElse(name, throw new RuntimeException("No such local"))
}
