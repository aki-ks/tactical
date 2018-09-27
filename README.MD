# Tactical

This project aims to simplify working with JVM bytecode.

Building a referenced based intermediation for java bytecode is the main objective.

# Objectives
* [X] implement basic classfile structure shared between all intermediations
* [X] build basic stack intermediation
* [ ] build basic reference based intermediation
* [X] conversion: bytecode ([asm](https://asm.ow2.io/)) <-> stack
* [ ] conversion: stack <-> reference
* [ ] implement the whole [jvm classfile format](https://docs.oracle.com/javase/specs/jvms/se10/html/index.html)
  * [X] for the stack intermediation (jvm 10)
  * [ ] for the reference based intermediation

## Future Goals
* [ ] support dalvik bytecode (android apps)
  * [ ] build dalvik intermediation
  * [ ] conversion: dex (bytecode) <-> dalvik (intermediation)
  * [ ] conversion: dalvik <-> reference
* [ ] Turn this project into a disassambler generating human editable textfiles
  * [ ] implement textifier
  * [ ] implement parser