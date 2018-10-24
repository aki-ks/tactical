package me.aki.tactical.core.parser.test

import java.util.{HashSet, LinkedHashMap, Optional}

import scala.collection.JavaConverters._
import me.aki.tactical.core.{Attribute, Field, FieldRef, Method, MethodRef, Module, Path}
import me.aki.tactical.core.`type`._
import me.aki.tactical.core.annotation._
import me.aki.tactical.core.constant._
import me.aki.tactical.core.handle._
import me.aki.tactical.core.typeannotation.TargetType._
import me.aki.tactical.core.typeannotation._
import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaChar, numChar}

object CoreGenerator {
  def literal = {
    val randomString = Arbitrary.arbitrary[String]
    val legalJavaLiteral = for {
      x ← Gen.alphaChar
      xs ← Gen.listOf(Gen.frequency(2 → numChar, 10 → alphaChar, 1 → Gen.oneOf('$', '_')))
    } yield (x :: xs).mkString
    Gen.frequency(10 → legalJavaLiteral, 1 → randomString)
  }

  // TYPES

  def primitiveType = Gen.oneOf(BooleanType.getInstance, ByteType.getInstance,
    ShortType.getInstance, CharType.getInstance, IntType.getInstance, LongType.getInstance,
    FloatType.getInstance, DoubleType.getInstance)

  def path = for {
    size ← Gen.choose(1, 5)
    literals ← Gen.listOfN(size, literal)
  } yield new Path(literals.init.asJava, literals.last)

  def objectType = for (path ← path) yield new ObjectType(path)
  def arrayType = for {
    baseType ← Gen.frequency(2 → primitiveType, 1 → objectType)
    dimensions ← Gen.choose(1, 5)
  } yield new ArrayType(baseType, dimensions)
  def refType = Gen.oneOf(objectType, arrayType)
  def typ = Gen.frequency(2 → primitiveType, 1 → objectType, 1 → arrayType)
  def returnType = for (typOpt ← Gen.option(typ)) yield typOpt.asJava

  def fieldRef = for {
    owner ← path
    name ← literal
    typ ← typ
  } yield new FieldRef(owner, name, typ)

  def methodRef = for {
    owner ← path
    name ← literal
    arguments ← Gen.listOf(typ)
    returnType ← returnType
  } yield new MethodRef(owner, name, arguments.asJava, returnType)

  // HANDLES

  def fieldHandle = Gen.oneOf(
    for (field ← fieldRef) yield new GetFieldHandle(field),
    for (field ← fieldRef) yield new GetStaticHandle(field),
    for (field ← fieldRef) yield new SetFieldHandle(field),
    for (field ← fieldRef) yield new SetStaticHandle(field)
  )

  def methodHandle = Gen.oneOf(
    for (method ← methodRef) yield new InvokeInterfaceHandle(method),
    for (method ← methodRef; iface ← arbitrary[Boolean]) yield new InvokeSpecialHandle(method, iface),
    for (method ← methodRef; iface ← arbitrary[Boolean]) yield new InvokeStaticHandle(method, iface),
    for (method ← methodRef) yield new InvokeVirtualHandle(method),
    for (method ← methodRef) yield new NewInstanceHandle(method),
  )

  def handle = Gen.oneOf(methodHandle, fieldHandle)

  // CONSTANTS

  def nullConstant = Gen.const(NullConstant.getInstance)
  def intConstant = for (int ← arbitrary[Int]) yield new IntConstant(int)
  def longConstant = for (long ← arbitrary[Long]) yield new LongConstant(long)
  def floatConstant = for (float ← arbitrary[Float]) yield new FloatConstant(float)
  def doubleConstant = for (double ← arbitrary[Double]) yield new DoubleConstant(double)
  def stringConstant = for (string ← arbitrary[String]) yield new StringConstant(string)
  def classConstant = for (typ ← refType) yield new ClassConstant(typ)
  def methodTypeConstant = for {
    paramTypes ← Gen.listOf(typ)
    returnType ← returnType
  } yield new MethodTypeConstant(paramTypes.asJava, returnType)

  def handleConstant = for (handle ← handle) yield new HandleConstant(handle)

  def dynamicConstant = for {
    name ← literal
    typ ← typ
    bootstrap ← handle
    bootstrapArgs ← Gen.listOf(bootstrapConstant)
  } yield new DynamicConstant(name, typ, bootstrap, bootstrapArgs.asJava)

  def fieldConstant: Gen[FieldConstant] =
    Gen.oneOf(intConstant, longConstant, floatConstant, doubleConstant, stringConstant)

  def bootstrapConstant: Gen[BootstrapConstant] =
    Gen.oneOf(intConstant, longConstant, floatConstant, doubleConstant, stringConstant,
      classConstant, methodTypeConstant, handleConstant)

  def pushableConstant: Gen[PushableConstant] =
    Gen.oneOf(nullConstant, intConstant, longConstant, floatConstant, doubleConstant,
      stringConstant,classConstant, methodTypeConstant, handleConstant, dynamicConstant)

  def primitiveAnnotationValue = Gen.oneOf(
    for (boolean ← arbitrary[Boolean]) yield new BooleanAnnotationValue(boolean),
    for (byte ← arbitrary[Byte]) yield new ByteAnnotationValue(byte),
    for (short ← arbitrary[Short]) yield new ShortAnnotationValue(short),
    for (char ← arbitrary[Char]) yield new CharAnnotationValue(char),
    for (int ← arbitrary[Int]) yield new IntAnnotationValue(int),
    for (long ← arbitrary[Long]) yield new LongAnnotationValue(long),
    for (float ← arbitrary[Float]) yield new FloatAnnotationValue(float),
    for (double ← arbitrary[Double]) yield new DoubleAnnotationValue(double)
  )

  def stringAnnotationValue = for (string ← arbitrary[String]) yield new StringAnnotationValue(string)
  def classAnnotationValue = for (typ ← typ) yield new ClassAnnotationValue(typ)
  def enumAnnotationValue = for {
    owner ← path
    constant ← literal
  } yield new EnumAnnotationValue(owner, constant)

  def annotationAnnotationValue: Gen[AnnotationAnnotationValue] = for {
    path ← path
    values ← Gen.mapOf(Gen.zip(literal, annotationValue))
  } yield new AnnotationAnnotationValue(path, new LinkedHashMap[String, AnnotationValue](values.asJava))

  def arrayAnnotationValue =
    for (values ← Gen.listOf(Gen.oneOf(primitiveAnnotationValue, stringAnnotationValue, classAnnotationValue, enumAnnotationValue)))
      yield new ArrayAnnotationValue(values.asJava)

  def annotationValue = Gen.oneOf(primitiveAnnotationValue, stringAnnotationValue, classAnnotationValue, enumAnnotationValue, arrayAnnotationValue)

  def annotation = for {
    typ ← path
    visible ← arbitrary[Boolean]
    values ← Gen.mapOf(Gen.zip(literal, annotationValue))
  } yield new Annotation(typ, visible, new LinkedHashMap[String, AnnotationValue](values.asJava))

  def attribute = for {
    name ← literal
    data ← arbitrary[Array[Byte]]
  } yield new Attribute(name, data)

  // TYPE ANNOTATIONS

  def typePathKind: Gen[TypePath.Kind]  = Gen.oneOf(
    Gen.const(new TypePath.Kind.Array),
    Gen.const(new TypePath.Kind.InnerClass),
    for (arg ← arbitrary[Int]) yield new TypePath.Kind.TypeArgument(arg),
    Gen.const(new TypePath.Kind.WildcardBound))

  def typePath = for (kinds ← Gen.listOf(typePathKind)) yield new TypePath(kinds.asJava)

  def classTargetType: Gen[TargetType.ClassTargetType] = Gen.oneOf(
    Gen.const(new TargetType.Extends),
    for (iface ← arbitrary[Int]) yield new TargetType.Implements(iface),
    for (typParam ← arbitrary[Int]) yield new TargetType.TypeParameter(typParam),
    for (typParam ← arbitrary[Int]; bound ← arbitrary[Int]) yield new TargetType.TypeParameterBound(typParam, bound)
  )

  def methodTargetType: Gen[TargetType.MethodTargetType] = Gen.oneOf(
    for (exception ← arbitrary[Int]) yield new TargetType.CheckedException(exception),
    for (parameter ← arbitrary[Int]) yield new TargetType.MethodParameter(parameter),
    Gen.const(new TargetType.MethodReceiver),
    Gen.const(new TargetType.ReturnType),
    for (typParam ← arbitrary[Int]) yield new TargetType.TypeParameter(typParam),
    for (typParam ← arbitrary[Int]; bound ← arbitrary[Int]) yield new TargetType.TypeParameterBound(typParam, bound)
  )

  def insnTargetType: Gen[TargetType.InsnTargetType] = Gen.oneOf(
    Gen.const(new TargetType.InstanceOf),
    Gen.const(new TargetType.New),
    Gen.const(new TargetType.ConstructorReference),
    Gen.const(new TargetType.MethodReference),
    for (intersection ← arbitrary[Int]) yield new TargetType.Cast(intersection),
    for (typParam ← arbitrary[Int]) yield new ConstructorInvokeTypeParameter(typParam),
    for (typParam ← arbitrary[Int]) yield new MethodInvokeTypeParameter(typParam),
    for (typParam ← arbitrary[Int]) yield new ConstructorReferenceTypeParameter(typParam),
    for (typParam ← arbitrary[Int]) yield new MethodReferenceTypeParameter(typParam)
  )

  def localTargetType: Gen[TargetType.LocalTargetType] = Gen.oneOf(
    Gen.const(new TargetType.LocalVariable),
    Gen.const(new ResourceVariable)
  )

  def classTypeAnnotation = for {
    typePath ← typePath
    annotation ← annotation
    targetType ← classTargetType
  } yield new ClassTypeAnnotation(typePath, annotation, targetType)

  def fieldTypeAnnotation = for {
    typePath ← typePath
    annotation ← annotation
  } yield new FieldTypeAnnotation(typePath, annotation)

  def methodTypeAnnotation = for {
    typePath ← typePath
    annotation ← annotation
    targetType ← methodTargetType
  } yield new MethodTypeAnnotation(typePath, annotation, targetType)

  def insnTypeAnnotation = for {
    typePath ← typePath
    annotation ← annotation
    targetType ← insnTargetType
  } yield new InsnTypeAnnotation(typePath, annotation, targetType)

  def localTypeAnnotation = for {
    typePath ← typePath
    annotation ← annotation
    targetType ← localTargetType
  } yield new LocalVariableTypeAnnotation(typePath, annotation, targetType)

  // MODULE

  def flags[F](values: Array[F]) = for (flags ← Gen.listOf(Gen.oneOf(values))) yield new HashSet(flags.asJava)

  def moduleRequire = for {
    name ← path
    flags ← flags(Module.Require.Flag.values)
    version ← Gen.option(literal)
  } yield new Module.Require(name, flags, version.asJava)

  def moduleExport = for {
    name ← path
    flags ← flags(Module.Export.Flag.values)
    modules ← Gen.listOf(path)
  } yield new Module.Export(name, flags, modules.asJava)

  def moduleOpen = for {
    name ← path
    flags ← flags(Module.Open.Flag.values)
    modules ← Gen.listOf(path)
  } yield new Module.Open(name, flags, modules.asJava)

  def moduleProvide = for {
    service ← path
    providers ← Gen.listOf(path)
  } yield new Module.Provide(service, providers.asJava)

  def module = for {
    name ← path
    flags ← flags(Module.Flag.values)
    version ← Gen.option(literal)
    mainClass ← Gen.option(path)
    packages ← Gen.listOf(path)
    require ← Gen.listOf(moduleRequire)
    exports ← Gen.listOf(moduleExport)
    open ← Gen.listOf(moduleOpen)
    uses ← Gen.listOf(path)
    provides ← Gen.listOf(moduleProvide)
  } yield new Module(name, flags, version.asJava, mainClass.asJava, packages.asJava, require.asJava,
    exports.asJava, open.asJava, uses.asJava, provides.asJava)

  // FIELD

  /** Get a set of flags that contains only one or non access flag */
  def accessFlags[F](accessFlags: Seq[F], other: Seq[F]) = for {
    access ← Gen.option(Gen.oneOf(accessFlags))
    otherFlags ← Gen.listOf(Gen.oneOf(other diff accessFlags))
  } yield new HashSet((otherFlags ++ access).asJava)

  def field = for {
    flags ← accessFlags(Seq(Field.Flag.PUBLIC, Field.Flag.PRIVATE, Field.Flag.PROTECTED), Field.Flag.values)
    name ← literal
    typ ← typ
    signature ← Gen.option(literal)
    value ← Gen.option(fieldConstant)
    annotations ← Gen.listOf(annotation)
    typeAnnotations ← Gen.listOf(fieldTypeAnnotation)
    attributes ← Gen.listOf(attribute)
  } yield new Field(flags, name, typ, signature.asJava, value.asJava, annotations.asJava, typeAnnotations.asJava, attributes.asJava)
}
