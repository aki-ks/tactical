package me.aki.tactical.ref.parser

import me.aki.tactical.core.util.Cell
import me.aki.tactical.ref.{RefLocal, Statement}

abstract class RefCtx() {
  protected val locals: Map[String, RefLocal]

  def getLocal(name: String): RefLocal =
    locals.getOrElse(name, throw new NoSuchElementException(s"No such local '$name'"))
}

/** A conversion context that's still resolving labels */
class UnresolvedRefCtx(protected val locals: Map[String, RefLocal]) extends RefCtx {
  private var unresolvedReferences = Map[String, Set[Cell[Statement]]]()

  def registerReference(label: String, target: Cell[Statement]): Unit = {
    val otherCells = unresolvedReferences.getOrElse(label, Set())
    unresolvedReferences += label -> (otherCells + target)
  }

  def resolve(labels: Map[String, Statement]): ResolvedRefCtx = {
    for {
      (labelName, cells) ← unresolvedReferences
      val label = labels.getOrElse(labelName, throw new NoSuchElementException(s"No such label '$labelName'"))
      cell ← cells
    } cell.set(label)

    new ResolvedRefCtx(locals, labels)
  }
}

/** A conversion context where all labels have been resolved */
class ResolvedRefCtx(protected val locals: Map[String, RefLocal], labels: Map[String, Statement]) extends RefCtx {
  def getLabel(name: String): Statement =
    labels.getOrElse(name, throw new NoSuchElementException(s"No such label '$name'"))
}
