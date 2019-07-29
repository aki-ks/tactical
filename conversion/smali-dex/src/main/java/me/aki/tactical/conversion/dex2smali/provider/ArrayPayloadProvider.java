package me.aki.tactical.conversion.dex2smali.provider;

import org.jf.dexlib2.Format;
import org.jf.dexlib2.iface.instruction.formats.ArrayPayload;
import org.jf.dexlib2.immutable.instruction.ImmutableArrayPayload;

import java.util.List;

public class ArrayPayloadProvider implements InstructionProvider<ArrayPayload> {
    private final int elementWidth;
    private final List<Number> elements;

    public ArrayPayloadProvider(int elementWidth, List<Number> elements) {
        this.elementWidth = elementWidth;
        this.elements = elements;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of();
    }

    @Override
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.ArrayPayload;
    }

    @Override
    public ArrayPayload newInstance() {
        return new ImmutableArrayPayload(this.elementWidth, this.elements);
    }
}
