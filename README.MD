# tactical
This project aims to build a referenced based intermediation for java bytecode
as well as a slightly simplified stack based intermediation.

# objectives
* [ ] implement basic classfile structure shared between all intermediations
* [ ] build basic stack intermediation
* [ ] build basic reference based intermediation
* [ ] conversion: bytecode ([asm](https://asm.ow2.io/)) <-> stack
* [ ] conversion: stack <-> reference

## future goals
* [ ] implement the whole [jvm classfile format](https://docs.oracle.com/javase/specs/jvms/se10/html/index.html)
* [ ] support dalvik bytecode (android apps)
  * [ ] build dalvik intermediation
  * [ ] conversion: dex <-> dalvik
  * [ ] conversion: dalvik <-> reference
* [ ] Turn this project into a disassambler generating human editable textfiles
  * [ ] implement textifier
  * [ ] implement parser