# Tactical

This project aims to simplify working with JVM bytecode.

Building a referenced based intermediation for java bytecode is the main objective.

# Objectives
* [X] implement basic classfile structure shared between all intermediations
* [X] build stack intermediation
* [X] build reference based intermediation
* [ ] build dalvik intermediation
* [X] conversion: bytecode ([asm](https://asm.ow2.io/)) <-> stack
* [ ] conversion: stack <-> reference
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
