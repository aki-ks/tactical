package me.aki.tactical.core;

import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.ClassTypeAnnotation;

import java.util.List;
import java.util.Objects;
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
    private Set<Flag> accessFlags;

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
     * All field definitions of this classfile.
     */
    private List<Field> fields;

    /**
     * All method definitions of this classfile.
     */
    private List<Method> methods;

    /**
     * Annotations of this classfile.
     */
    private List<Annotation> annotations;

    /**
     * Annotations on types within this classfile.
     */
    private List<ClassTypeAnnotation> typeAnnotations;

    /**
     * Non-parsed attributes of this classfile.
     *
     * They are either not part of the JVM spec or
     * are not yet supported by this library.
     */
    private List<Attribute> attributes;

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

}
