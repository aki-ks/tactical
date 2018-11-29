# Tactical

This project aims to simplify working with JVM bytecode.

Building a referenced based intermediation for java bytecode is the main objective.

# Stack
- The `stack` intermediation represents bytecode close to JVM bytecode.
- Instructions that serve the sole purpose of saving diskspace have been removed.
  E.g. `bipush`, `sipush`, `ldc`, `ldc2`, `iconst_0`, `iconst_1` and many more are all represented by the `PushInsn`.
- `long` and `double` values require only one slot on the stack.
  The DUP2 instruction for instance does always duplicate two values.
  In JVM bytecode it may duplicate either one or two values depending on their type.
- Dead code will be dropped during the conversion

# Ref
- The `ref` intermediation drops the concept of operating on a stack and uses references instead.
- Instructions have been replaced against the concept of statements and expressions.
- The instruction set was thereby further reduced.
- It is a statically typed language - locals have types.
- Dead code will be dropped and debug information such as line numbers might be lost or altered during the conversion.


# Objectives
* [X] implement basic classfile structure shared between all intermediations
* [X] build stack intermediation
* [X] build reference based intermediation
* [ ] build dalvik intermediation
* [X] conversion: bytecode ([asm](https://asm.ow2.io/)) <-> stack
* [X] conversion: stack <-> reference
* [ ] conversion: dex (bytecode) <-> dalvik (intermediation)
* [ ] conversion: dalvik <-> reference
* [ ] implement the whole [jvm](https://docs.oracle.com/javase/specs/jvms/se11/html/index.html) / [dex](https://source.android.com/devices/tech/dalvik) classfile format
  * [X] for the stack intermediation (jvm 11)
  * [ ] for the reference based intermediation
  * [ ] for the dex intermediation
* [X] Turn this project into a disassambler generating human editable textfiles
  * [X] Textifier for core
  * [X] Textifier for stack
  * [X] Textifier for ref
  * [ ] Textifier for dex
  * [X] Parser for core
  * [X] Parser for stack
  * [X] Parser for ref
  * [ ] Parser for dex
