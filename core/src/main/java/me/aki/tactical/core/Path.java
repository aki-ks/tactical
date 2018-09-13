package me.aki.tactical.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Store the name of a classfile with its package.
 */
public class Path {
    /**
     * Packages of the the classfile
     */
    private List<String> pkg;

    /**
     * Name of the classfile
     */
    private String name;

    /**
     * Syntactic sugar for initialization of a path constant.
     *
     * example:
     * <code> Path.of("java", "lang", "String") </code>
     *
     * @param path the packages followed by the name of the class
     * @throws IllegalArgumentException
     */
    public static Path of(String... path) {
        if (path.length == 0) {
            throw new IllegalArgumentException();
        }

        String name = path[path.length - 1];

        List<String> pkg = Arrays.stream(path)
                .limit(path.length - 1)
                .collect(Collectors.toList());

        return new Path(pkg, name);
    }

    public Path(String[] pkg, String name) {
        this(new ArrayList<>(Arrays.asList(pkg)), name);
    }

    public Path(List<String> pkg, String name) {
        this.pkg = pkg;
        this.name = name;
    }

    public List<String> getPackage() {
        return pkg;
    }

    public void setPackage(List<String> pkg) {
        this.pkg = pkg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Path{" +
                "pkg=" + pkg +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(pkg, path.pkg) &&
                Objects.equals(name, path.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkg, name);
    }
}
