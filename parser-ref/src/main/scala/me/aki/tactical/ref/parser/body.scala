package me.aki.tactical.ref.parser

import java.util.Optional

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.`type`.{ObjectType, Type}
import me.aki.tactical.core.{Body, Classfile, Method}
import me.aki.tactical.core.parser.{Parser, _}
import me.aki.tactical.ref.{RefBody, RefLocal, TryCatchBlock}

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
    val thisLocal = if (method.getFlag(Method.Flag.STATIC)) None else Some(new RefLocal(new ObjectType(classfile.getName)))
    val paramLocals = for (typ ← method.getParameterTypes.asScala) yield new RefLocal(typ)

    P { (TypeParser ~ WS.? ~ Literal ~ WS.? ~ ";").rep(sep = WS.?) ~ WS.? }
      .map { _ map { case (typ, name) => (name, new RefLocal(typ)) } }
      .flatMap { locals =>
        val localMap = {
          val thisLocalMap = for (local ← thisLocal) yield ("this", local)
          val paramLocalMap = for (((typ, name), local) ← ctx.params zip paramLocals) yield (name, local)
          thisLocalMap ++ paramLocalMap ++ locals
        }.toMap

        val unresolvedCtx = new UnresolvedRefCtx(localMap)

        val stmtWithLabel = (Literal ~ WS.? ~ ":" ~ WS.?).? ~ new StatementParser(unresolvedCtx)
        P { stmtWithLabel.rep(sep = WS.?) } ~ WS.? flatMap { statementsWithLabels =>
          val resolvedCtx = {
            val labels = statementsWithLabels collect { case (Some(name), stmt) => (name, stmt) }
            unresolvedCtx.resolve(labels.toMap)
          }

          new BodyContentParser(resolvedCtx).rep(sep = WS.?) map { contents =>
            val body = new RefBody()
            for (local ← thisLocal) body.setThisLocal(Optional.of(local))
            for (local ← paramLocals) body.getArgumentLocals.add(local)
            for ((_, local) ← localMap) body.getLocals.add(local)
            for ((_, stmt) ← statementsWithLabels) body.getStatements.add(stmt)
            for (content ← contents) content.apply(body)
            body
          }
        }
      }
  }
}

sealed trait BodyContent extends (RefBody => Unit)
object BodyContent {
  case class TryCatch(block: TryCatchBlock) extends BodyContent {
    def apply(body: RefBody) = body.getTryCatchBlocks.add(block)
  }

  case class Line(line: RefBody.LineNumber) extends BodyContent {
    def apply(body: RefBody) = body.getLineNumbers.add(line)
  }

  case class LocalVariable(variable: RefBody.LocalVariable) extends BodyContent {
    def apply(body: RefBody) = body.getLocalVariables.add(variable)
  }

  case class LocalVariableAnnotation(anno: RefBody.LocalVariableAnnotation) extends BodyContent {
    def apply(body: RefBody) = body.getLocalVariableAnnotations.add(anno)
  }
}

class BodyContentParser(ctx: ResolvedRefCtx) extends Parser[BodyContent] {
  val parser: P[BodyContent] = P {
    new TryCatchBlockParser(ctx).map(BodyContent.TryCatch) |
    new LineParser(ctx).map(BodyContent.Line) |
    new LocalVariableParser(ctx).map(BodyContent.LocalVariable) |
    new LocalVariableAnnotationParser(ctx).map(BodyContent.LocalVariableAnnotation)
  }
}
