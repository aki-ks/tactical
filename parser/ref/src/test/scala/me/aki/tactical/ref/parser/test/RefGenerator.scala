package me.aki.tactical.ref.parser.test

import scala.collection.JavaConverters._
import me.aki.tactical.core.parser.test.CoreGenerator
import me.aki.tactical.ref.Expression
import me.aki.tactical.ref.expr._
import me.aki.tactical.ref.invoke._
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

object RefGenerator extends RefGenerator
trait RefGenerator extends CoreGenerator {
  def constantExpr = for (const ← CoreGenerator.pushableConstant) yield new ConstantExpr(const)
  def negExpr = for (value ← expr) yield new NegExpr(value)

  def binaryExpr = for {
    op1 ← expr
    op2 ← expr
    expr ← Gen.oneOf(
      new AddExpr(op1, op2), new SubExpr(op1, op2), new MulExpr(op1, op2), new DivExpr(op1, op2), new ModExpr(op1, op2),
      new AndExpr(op1, op2), new OrExpr(op1, op2), new XorExpr(op1, op2),
      new ShlExpr(op1, op2), new ShrExpr(op1, op2), new UShrExpr(op1, op2),
      new CmpExpr(op1, op2), new CmplExpr(op1, op2), new CmpgExpr(op1, op2))
  } yield expr

  def castExpr = for {
    typ ← typ
    value ← expr
  } yield new CastExpr(typ, value)

  def concreteInvoke = for {
    methodRef ← methodRef
    val argCount = methodRef.getArguments.size
    arguments ← Gen.listOfN(argCount, expr).map(_.asJava)
    invoke ← Gen.oneOf(
      for (instance ← expr) yield new InvokeInterface(methodRef, instance, arguments),
      for (instance ← expr; iface ← arbitrary[Boolean]) yield new InvokeSpecial(methodRef, instance, arguments, iface),
      for (iface ← arbitrary[Boolean]) yield new InvokeStatic(methodRef, arguments, iface),
      for (instance ← expr) yield new InvokeVirtual(methodRef, instance, arguments)
    )
  } yield invoke

  def dynamicInvoke = for {
    name ← literal
    descriptor ← methodDescriptor
    bootstrap ← handle
    bootstrapArguments ← Gen.listOf(bootstrapConstant)
    arguments ← Gen.listOfN(descriptor.getParameterTypes.size, expr)
  } yield new InvokeDynamic(name, descriptor, bootstrap, bootstrapArguments.asJava, arguments.asJava)

  def abstractInvoke = Gen.frequency(4 -> concreteInvoke, 1 -> dynamicInvoke)

  def invokeExpr = for (invoke ← abstractInvoke) yield new InvokeExpr(invoke)

  def staticFieldExpr = for (field ← fieldRef) yield new StaticFieldExpr(field)
  def instanceFieldExpr = for {
    field ← fieldRef
    instance ← expr
  } yield new InstanceFieldExpr(field, instance)

  def expr: Gen[Expression] = Gen.lzy(Gen.oneOf(constantExpr, negExpr, binaryExpr, castExpr))
}
