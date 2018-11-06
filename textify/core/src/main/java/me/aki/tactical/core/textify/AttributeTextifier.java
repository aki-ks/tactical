package me.aki.tactical.core.textify;

import me.aki.tactical.core.Attribute;

public class AttributeTextifier implements Textifier<Attribute> {
    private static final int LINE_LENGTH = 16;
    private static final char[] HEX_CHARS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private AttributeTextifier() {}

    private final static AttributeTextifier INSTANCE = new AttributeTextifier();

    public static AttributeTextifier getInstance() {
        return INSTANCE;
    }

    @Override
    public void textify(Printer printer, Attribute attribute) {
        printer.addText("attribute ");
        printer.addEscaped(attribute.getName(), '"');

        byte[] data = attribute.getData();
        if (data.length == 0) {
            printer.addText(" {}");
        } else {
            printer.addText(" {");
            printer.newLine();
            printer.increaseIndent();

            appendHexData(printer, data);

            printer.decreaseIndent();
            printer.addText("}");
        }
    }

    private void appendHexData(Printer printer, byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            toHexString(builder, data[i]);

            boolean isLastChar = i == data.length - 1;
            if (!isLastChar) {
                boolean isLastCharInLine = (i + 1) % LINE_LENGTH == 0;
                if (isLastCharInLine) {
                    printer.addText(builder.toString());
                    printer.newLine();
                    builder.setLength(0);
                } else {
                    builder.append(' ');
                }
            }
        }

        printer.addText(builder.toString());
        printer.newLine();
    }

    private void toHexString(StringBuilder builder, byte b) {
        builder.append(HEX_CHARS[b >>> 4 & 0xF]);
        builder.append(HEX_CHARS[b & 0xF]);
    }
}
