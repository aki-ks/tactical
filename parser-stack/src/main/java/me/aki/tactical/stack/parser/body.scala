package me.aki.tactical.stack.parser

import scala.collection.JavaConverters._
import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.{Body, Method}
import me.aki.tactical.core.`type`.Type
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation
import me.aki.tactical.stack.StackBody.{LineNumber, LocalVariable, LocalVariableAnnotation}
import me.aki.tactical.stack.insn.Instruction
import me.aki.tactical.stack.parser.BodySuffix._
import me.aki.tactical.stack.{StackBody, StackLocal, TryCatchBlock}

case class StackBodyArgumentCtx(params: List[(Type, String)])

object StackBodyParser extends StackBodyParser
class StackBodyParser extends BodyParser {
  type Ctx = StackBodyArgumentCtx

  sealed trait LocalValue
  case object This extends LocalValue
  case class Param(i: Int) extends LocalValue

  def staticInitializerCtx = StackBodyArgumentCtx(Nil)

  def parameterParser: P[(Ctx, List[Type])] = P {
    val params = TypeParser ~ WS ~ Literal
    for (params ← params.rep(sep = WS.? ~ "," ~ WS.?)) yield {
      val paramTypes = for ((param, _) ← params) yield param
      val ctx = StackBodyArgumentCtx(params.toList)
      (ctx, paramTypes.toList)
    }
  }

  def bodyParser(method: Method, ctx: Ctx): P[Body] = P {
    val unresolvedCtx = new UnresolvedStackCtx()

    val thisLocalOpt = if (method.getFlag(Method.Flag.STATIC)) None else Some(unresolvedCtx.getLocal("this"))
    val paramLocals = for ((typ, arg) ← ctx.params) yield unresolvedCtx.getLocal(arg)

    val instructions = P {
      val insnParser = new InsnParser(unresolvedCtx)
      val label = Literal ~ WS.? ~ ":" ~ WS.?
      (label ~ WS.?).? ~ insnParser
    }.rep(sep = WS.?)

    (instructions ~ WS.?).flatMap { insnsWithLabels =>
      val resolvedCtx = {
        val labels = insnsWithLabels.collect { case (Some(labelName), insn) => (labelName, insn) }.toMap
        unresolvedCtx.resolve(labels)
      }

      for (suffixes ← new BoddySuffixParser(resolvedCtx).rep(sep = WS.?)) yield {
        val body = new StackBody()

        body.setThisLocal(Optional.ofNullable(thisLocalOpt.orNull))
        body.setParameterLocals(paramLocals.asJava)

        for ((_, local) ← resolvedCtx.locals) body.getLocals.add(local)
        for ((_, insn) ← insnsWithLabels) body.getInstructions.add(insn)
        for (suffix ← suffixes) suffix.apply(body)

        body
      }
    }
  }
}

sealed trait BodySuffix extends (StackBody => Unit)
object BodySuffix {
  case class TryCatchSuffix(block: TryCatchBlock) extends BodySuffix {
    def apply(body: StackBody): Unit =
      body.getTryCatchBlocks.add(block)
  }

  case class LineNumberSuffix(line: LineNumber) extends BodySuffix {
    def apply(body: StackBody): Unit =
      body.getLineNumbers.add(line)
  }

  case class LocalVariableSuffix(local: LocalVariable) extends BodySuffix {
    def apply(body: StackBody): Unit =
      body.getLocalVariables.add(local)
  }

  case class LocalVariableAnnotationSuffix(local: LocalVariableAnnotation) extends BodySuffix {
    def apply(body: StackBody): Unit =
      body.getLocalVariableAnnotations.add(local)
  }

  case class InsnAnnotationSuffix(insn: Instruction, annotation: InsnTypeAnnotation) extends BodySuffix {
    def apply(body: StackBody): Unit =
      insn.getTypeAnnotations.add(annotation)
  }
}

class BoddySuffixParser(ctx: ResolvedStackCtx) extends Parser[BodySuffix] {
  val parser: P[BodySuffix] = P {
    new TryCatchBlockParser(ctx).map(TryCatchSuffix) |
    new LineNumberParser(ctx).map(LineNumberSuffix) |
    new LocalVariableParser(ctx).map(LocalVariableSuffix) |
    new LocalVariableAnnotationParser(ctx).map(LocalVariableAnnotationSuffix) |
    new InsnAnnotationParser(ctx).map { case (insn, anno) => InsnAnnotationSuffix(insn, anno)}
  }
}
