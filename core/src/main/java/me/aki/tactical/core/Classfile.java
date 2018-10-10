package me.aki.tactical.core;

import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.ClassTypeAnnotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the content of a whole "*.class" file.
 */
public class Classfile {
    /**
     * Major and minor version of this classfile.
     */
    private Version version;

    /**
     * Access flags of this classfile
     */
    private Set<Flag> accessFlags = new HashSet<>();

    /**
     * Package and name of the classfile
     */
    private Path name;

    /**
     * {@link Path} of the supertype of this class.
     *
     * May be {@code null} when reading the bytecode of class {@link java.lang.Object}.
     */
    private Path supertype;

    /**
     * {@link Path} of each interfaces implemented by this classfile.
     */
    private List<Path> interfaces;

    /**
     * Class signature with type variables.
     */
    private Optional<String> signature = Optional.empty();

    /**
     * Name of file from which the classfile was compiled.
     */
    private Optional<String> source = Optional.empty();

    /**
     * Additional debug informations that compilers may store within a classfile.
     */
    private Optional<String> sourceDebug = Optional.empty();

    /**
     * Classes declared within this class.
     */
    private List<InnerClass> innerClasses = new ArrayList<>();

    /**
     * Method in the enclosing class that contains this class.
     */
    private Optional<EnclosingMethod> enclosingMethod = Optional.empty();

    /**
     * Name of another class in the same package that nests this class.
     *
     * This class has access to all private members of the nesting class.
     */
    private Optional<Path> nestHost = Optional.empty();

    /**
     * Names of all classes that are nested within this class.
     *
     * The {@link Classfile#nestHost} of all nested classes must be
     * set to the {@link Classfile#name} of this class.
     */
    private List<Path> nestMembers = new ArrayList<>();

    /**
     * Module definition if the classfile is a "module-info".
     */
    private Optional<Module> module = Optional.empty();

    /**
     * All field definitions of this classfile.
     */
    private List<Field> fields = new ArrayList<>();

    /**
     * All method definitions of this classfile.
     */
    private List<Method> methods = new ArrayList<>();

    /**
     * Annotations of this classfile.
     */
    private List<Annotation> annotations = new ArrayList<>();

    /**
     * Annotations on types within this classfile.
     */
    private List<ClassTypeAnnotation> typeAnnotations = new ArrayList<>();

    /**
     * Non-parsed attributes of this classfile.
     *
     * They are either not part of the JVM spec or
     * are not yet supported by this library.
     */
    private List<Attribute> attributes = new ArrayList<>();

    public Classfile(Version version, Path name, Path supertype, List<Path> interfaces) {
        this.version = version;
        this.name = name;
        this.supertype = supertype;
        this.interfaces = interfaces;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Path getName() {
        return name;
    }

    public void setName(Path name) {
        this.name = name;
    }

    public Path getSupertype() {
        return supertype;
    }

    public void setSupertype(Path supertype) {
        this.supertype = supertype;
    }

    public List<Path> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Path> interfaces) {
        this.interfaces = interfaces;
    }

    public Optional<String> getSignature() {
        return signature;
    }

    public void setSignature(Optional<String> signature) {
        this.signature = signature;
    }

    public Optional<String> getSource() {
        return source;
    }

    public void setSource(Optional<String> source) {
        this.source = source;
    }

    public Optional<String> getSourceDebug() {
        return sourceDebug;
    }

    public void setSourceDebug(Optional<String> sourceDebug) {
        this.sourceDebug = sourceDebug;
    }

    public List<InnerClass> getInnerClasses() {
        return innerClasses;
    }

    public void setInnerClasses(List<InnerClass> innerClasses) {
        this.innerClasses = innerClasses;
    }

    public Optional<EnclosingMethod> getEnclosingMethod() {
        return enclosingMethod;
    }

    public void setEnclosingMethod(Optional<EnclosingMethod> enclosingMethod) {
        this.enclosingMethod = enclosingMethod;
    }

    public Optional<Path> getNestHost() {
        return nestHost;
    }

    public void setNestHost(Optional<Path> nestHost) {
        this.nestHost = nestHost;
    }

    public List<Path> getNestMembers() {
        return nestMembers;
    }

    public void setNestMembers(List<Path> nestMembers) {
        this.nestMembers = nestMembers;
    }

    public Optional<Module> getModule() {
        return module;
    }

    public void setModule(Optional<Module> module) {
        this.module = module;
    }

    public Set<Flag> getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(Set<Flag> accessFlags) {
        this.accessFlags = accessFlags;
    }

    /**
     * Check whether a {@link Flag} is set for this classfile.
     *
     * @param flag flag to check for
     * @return is the flag set
     */
    public boolean getFlag(Flag flag) {
        return accessFlags.contains(flag);
    }

    /**
     * Set/remove a {@link Flag}.
     *
     * @param flag to be set/removed
     * @param shouldSet should the flag be set or removed
     */
    public void setFlag(Flag flag, boolean shouldSet) {
        if (shouldSet) {
            accessFlags.add(flag);
        } else {
            accessFlags.remove(flag);
        }
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public List<ClassTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<ClassTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Classfile classfile = (Classfile) o;
        return Objects.equals(version, classfile.version) &&
                Objects.equals(accessFlags, classfile.accessFlags) &&
                Objects.equals(name, classfile.name) &&
                Objects.equals(supertype, classfile.supertype) &&
                Objects.equals(interfaces, classfile.interfaces) &&
                Objects.equals(signature, classfile.signature) &&
                Objects.equals(source, classfile.source) &&
                Objects.equals(sourceDebug, classfile.sourceDebug) &&
                Objects.equals(innerClasses, classfile.innerClasses) &&
                Objects.equals(enclosingMethod, classfile.enclosingMethod) &&
                Objects.equals(nestHost, classfile.nestHost) &&
                Objects.equals(nestMembers, classfile.nestMembers) &&
                Objects.equals(module, classfile.module) &&
                Objects.equals(fields, classfile.fields) &&
                Objects.equals(methods, classfile.methods) &&
                Objects.equals(annotations, classfile.annotations) &&
                Objects.equals(typeAnnotations, classfile.typeAnnotations) &&
                Objects.equals(attributes, classfile.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, accessFlags, name, supertype, interfaces, signature, source,
                sourceDebug, innerClasses, enclosingMethod, nestHost, nestMembers, module, fields,
                methods, annotations, typeAnnotations, attributes);
    }

    @Override
    public String toString() {
        return Classfile.class.getSimpleName() + '{' +
                "version=" + version +
                ", accessFlags=" + accessFlags +
                ", name=" + name +
                ", supertype=" + supertype +
                ", interfaces=" + interfaces +
                ", signature=" + signature +
                ", source=" + source +
                ", sourceDebug=" + sourceDebug +
                ", innerClasses=" + innerClasses +
                ", enclosingMethod=" + enclosingMethod +
                ", nestHost=" + nestHost +
                ", nestMembers=" + nestMembers +
                ", module=" + module +
                ", fields=" + fields +
                ", methods=" + methods +
                ", annotations=" + annotations +
                ", typeAnnotations=" + typeAnnotations +
                ", attributes=" + attributes +
                '}';
    }

    /**
     * Version of a classfile mostly denoted as "[major].[minor]" (e.g. 52.0)
     */
    public static class Version {
        public static int MAJOR_JDK_1 = 45;
        public static int MAJOR_JDK_2 = 46;
        public static int MAJOR_JDK_3 = 47;
        public static int MAJOR_JDK_4 = 48;
        public static int MAJOR_JDK_5 = 49;
        public static int MAJOR_JDK_6 = 50;
        public static int MAJOR_JDK_7 = 51;
        public static int MAJOR_JDK_8 = 52;
        public static int MAJOR_JDK_9 = 53;
        public static int MAJOR_JDK_10 = 54;

        private int major;
        private int minor;

        public Version(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        public int getMajor() {
            return major;
        }

        public void setMajor(int major) {
            this.major = major;
        }

        public int getMinor() {
            return minor;
        }

        public void setMinor(int minor) {
            this.minor = minor;
        }

        @Override
        public String toString() {
            return major + "." + minor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Version version = (Version) o;
            return major == version.major &&
                    minor == version.minor;
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor);
        }
    }

    public static enum Flag {
        PUBLIC, FINAL, SUPER, INTERFACE, ABSTRACT, SYNTHETIC, ANNOTATION, ENUM, MODULE
    }

    public static class EnclosingMethod {
        /**
         * Name of the class that contains the enclosing method.
         */
        private Path owner;

        /**
         * Name of the method that contains the class.
         */
        private Optional<String> name;

        /**
         * Return type and parameters of the method.
         */
        private Optional<MethodDescriptor> descriptor;

        public EnclosingMethod(Path owner) {
            this(owner, Optional.empty(), Optional.empty());
        }

        public EnclosingMethod(Path owner, Optional<String> name, Optional<MethodDescriptor> descriptor) {
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }

        public Path getOwner() {
            return owner;
        }

        public void setOwner(Path owner) {
            this.owner = owner;
        }

        public Optional<String> getName() {
            return name;
        }

        public void setName(Optional<String> name) {
            this.name = name;
        }

        public Optional<MethodDescriptor> getDescriptor() {
            return descriptor;
        }

        public void setDescriptor(Optional<MethodDescriptor> descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnclosingMethod that = (EnclosingMethod) o;
            return Objects.equals(owner, that.owner) &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(descriptor, that.descriptor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, name, descriptor);
        }

        @Override
        public String toString() {
            return "EnclosingMethod{" +
                    "owner='" + owner + '\'' +
                    ", name=" + name +
                    ", descriptor=" + descriptor +
                    '}';
        }
    }

    public static class InnerClass {
        /**
         * Classfile name of the inner class.
         */
        private Path name;

        /**
         * Name of the class that contains the inner class.
         */
        private Optional<Path> outerName;

        /**
         * Name of the inner class as declared in source.
         */
        private Optional<String> innerName;

        /**
         * Access flags of the inner class as declared in source.
         */
        private Set<Flag> flags;

        public InnerClass(Path name) {
            this(name, Optional.empty(), Optional.empty(), new HashSet<>());
        }

        public InnerClass(Path name, Optional<Path> outerName, Optional<String> innerName, Set<Flag> flags) {
            this.name = name;
            this.outerName = outerName;
            this.innerName = innerName;
            this.flags = flags;
        }

        public Path getName() {
            return name;
        }

        public void setName(Path name) {
            this.name = name;
        }

        public Optional<Path> getOuterName() {
            return outerName;
        }

        public void setOuterName(Optional<Path> outerName) {
            this.outerName = outerName;
        }

        public Optional<String> getInnerName() {
            return innerName;
        }

        public void setInnerName(Optional<String> innerName) {
            this.innerName = innerName;
        }

        public Set<Flag> getFlags() {
            return flags;
        }

        public void setFlags(Set<Flag> flags) {
            this.flags = flags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InnerClass that = (InnerClass) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(outerName, that.outerName) &&
                    Objects.equals(innerName, that.innerName) &&
                    Objects.equals(flags, that.flags);
        }

        @Override
        public String toString() {
            return InnerClass.class.getSimpleName() + '{' +
                    "name='" + name + '\'' +
                    ", outerName=" + outerName +
                    ", innerName=" + innerName +
                    ", flags=" + flags +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, outerName, innerName, flags);
        }

        public static enum Flag {
            PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, INTERFACE, ABSTRACT, SYNTHETIC, ANNOTATION, ENUM
        }
    }
}
