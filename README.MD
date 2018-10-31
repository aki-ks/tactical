# Tactical

This project aims to simplify working with JVM bytecode.

Building a referenced based intermediation for java bytecode is the main objective.

# Objectives
* [X] implement basic classfile structure shared between all intermediations
* [X] build basic stack intermediation
* [X] build basic reference based intermediation
* [X] conversion: bytecode ([asm](https://asm.ow2.io/)) <-> stack
* [ ] conversion: stack <-> reference
* [ ] implement the whole [jvm classfile format](https://docs.oracle.com/javase/specs/jvms/se11/html/index.html)
  * [X] for the stack intermediation (jvm 11)
  * [ ] for the reference based intermediation
* [X] Turn this project into a disassambler generating human editable textfiles
  * [X] Textifier for core
  * [X] Textifier for stack
  * [X] Textifier for ref
  * [X] Parser for core
  * [X] Parser for stack
  * [X] Parser for ref

## Future Goals
* [ ] support dalvik bytecode (android apps)
  * [ ] build dalvik intermediation
  * [ ] conversion: dex (bytecode) <-> dalvik (intermediation)
  * [ ] conversion: dalvik <-> reference
