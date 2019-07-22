package me.aki.tactical.core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable name of a class and its package.
 */
public class Path {
    public static final Path OBJECT = Path.of("java", "lang", "Object");
    public static final Path STRING = Path.of("java", "lang", "String");
    public static final Path CLASS = Path.of("java", "lang", "Class");
    public static final Path THROWABLE = Path.of("java", "lang", "Throwable");

    /**
     * Packages of the class stored in an immutable list.
     */
    private final List<String> pkg;

    /**
     * Name of the class
     */
    private final String name;

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
                .collect(Collectors.toUnmodifiableList());

        return new Path(pkg, name);
    }

    public Path(String[] pkg, String name) {
        this.pkg = List.of(pkg);
        this.name = name;
    }

    public Path(List<String> pkg, String name) {
        this.pkg = List.copyOf(pkg);
        this.name = name;
    }

    public List<String> getPackage() {
        return pkg;
    }

    public String getName() {
        return name;
    }

    /**
     * Join the package and class name.
     *
     * @param separator char separating each package and the name
     * @return joined path
     */
    public String join(char separator) {
        StringBuilder builder = new StringBuilder();
        for (String pkg : this.pkg) {
            builder.append(pkg);
            builder.append(separator);
        }
        builder.append(this.name);
        return builder.toString();
    }

    @Override
    public String toString() {
        String joined = Stream.concat(pkg.stream(), Stream.of(name))
                .map(p -> '\'' + p + '\'')
                .collect(Collectors.joining(", "));

        return "Path{" + joined + '}';
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
