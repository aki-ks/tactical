package me.aki.tactical.core.textify;

import me.aki.tactical.core.Path;

import java.util.ArrayList;
import java.util.List;

public class Printer {
    private final static String LINE_SEPARATOR = System.lineSeparator();
    private final static String INDENT_STRING = "  ";

    /**
     * List of all lines of the file.
     */
    private List<Line> lines;

    public Printer() {
        this.lines = new ArrayList<>(List.of(new Line(0)));
    }

    private Line getLastLine() {
        return this.lines.get(this.lines.size() - 1);
    }

    /**
     * Increase the indentation of the current line by one.
     */
    public void increaseIndent() {
        getLastLine().indent += 1;
    }

    /**
     * Decrease the indentation of the current line by one.
     */
    public void decreaseIndent() {
        Line lastLine = getLastLine();
        lastLine.indent -= 1;

        if (lastLine.indent < 0) {
            throw new IllegalStateException("Negative indent");
        }
    }

    /**
     * Start a new line
     */
    public void newLine() {
        int indent = getLastLine().indent;
        this.lines.add(new Line(indent));
    }

    /**
     * Append an unescaped string to the current line.
     *
     * @param text to be appended
     */
    public void addText(String text) {
        add(new BasicText(text));
    }

    /**
     * Append a string to the current line that gets escaped if necessary.
     *
     * @param text to be appended
     */
    public void addLiteral(String text) {
        add(new Literal(text));
    }

    /**
     * Append a Path to the current line.
     *
     * @param path that gets appended
     */
    public void addPath(Path path) {
        add(new PathToken(path));
    }

    /**
     * Append a token to the current line.
     *
     * @param token that is added to the line
     */
    protected void add(Token token) {
        getLastLine().tokens.add(token);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Line line : this.lines) {
            appendIndent(builder, line.indent);

            for (Token token : line.tokens) {
                token.append(builder);
            }

            builder.append(LINE_SEPARATOR);
        }

        return builder.toString();
    }

    private void appendIndent(StringBuilder builder, int indent) {
        builder.ensureCapacity(INDENT_STRING.length() * indent);
        for (int i = 0; i < indent; i++) {
            builder.append(INDENT_STRING);
        }
    }

    private class Line {
        /**
         * The indentation of this line.
         */
        private int indent;

        /**
         * All tokens that form the content of this line.
         */
        private List<Token> tokens = new ArrayList<>();

        public Line(int indent) {
            this.indent = indent;
        }
    }

    private interface Token {
        void append(StringBuilder builder);
    }

    private class BasicText implements Token {
        private String text;

        public BasicText(String text) {
            this.text = text;
        }

        @Override
        public void append(StringBuilder builder) {
            builder.append(text);
        }
    }

    private class Literal implements Token {
        private String text;

        public Literal(String text) {
            this.text = text;
        }

        @Override
        public void append(StringBuilder builder) {
            EscapeUtil.printMaybeEscapedString(builder, text);
        }
    }

    private class PathToken implements Token {
        private Path path;

        public PathToken(Path path) {
            this.path = path;
        }

        @Override
        public void append(StringBuilder builder) {
            for (String pkg : path.getPackage()) {
                EscapeUtil.printMaybeEscapedString(builder, pkg);
                builder.append('.');
            }
            EscapeUtil.printMaybeEscapedString(builder, path.getName());
        }
    }
}
