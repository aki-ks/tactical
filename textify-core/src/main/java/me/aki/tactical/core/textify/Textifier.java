package me.aki.tactical.core.textify;

@FunctionalInterface
public interface Textifier<T> {
    public void textify(Printer printer, T value);

    default String toString(T value) {
        Printer printer = new Printer();
        textify(printer, value);
        return printer.toString();
    }
}
