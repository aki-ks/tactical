package me.aki.tactical.core;

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
    private Set<Flag> access;

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
     * All attributes of this classfile that could not be parsed
     */
    private List<Attribute> attributes;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Set<Flag> getAccess() {
        return access;
    }

    public void setAccess(Set<Flag> access) {
        this.access = access;
    }

    public Path getName() {
        return name;
    }

    public void setName(Path name) {
        this.name = name;
    }

    public Set<Flag> getAccessFlags() {
        return access;
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

    /**
     * Check whether a {@link Flag} is set for this classfile.
     *
     * @param flag flag to check for
     * @return is the flag set
     */
    public boolean getFlag(Flag flag) {
        return access.contains(flag);
    }

    /**
     * Set/remove a {@link Flag}.
     *
     * @param flag to be set/removed
     * @param isSet should the flag be set or removed
     */
    public void setFlag(Flag flag, boolean isSet) {
        if (isSet) {
            access.add(flag);
        } else {
            access.remove(flag);
        }
    }

    public List<Attribute> getAttributes() {
        return attributes;
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
