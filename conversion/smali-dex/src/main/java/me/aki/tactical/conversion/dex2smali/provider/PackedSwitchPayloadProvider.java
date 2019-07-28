package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;
import org.jf.dexlib2.immutable.instruction.ImmutablePackedSwitchPayload;

import java.util.Map;
import java.util.TreeSet;

public class PackedSwitchPayloadProvider extends AbstractSwitchPayloadProvider<PackedSwitchPayload> {
    public PackedSwitchPayloadProvider(TreeSet<Integer> sortedKeys, Map<Integer, Instruction> branchTable) {
        super(sortedKeys, branchTable);
    }

    @Override
    public Format getFormat() {
        return Format.PackedSwitchPayload;
    }

    @Override
    public PackedSwitchPayload newInstance() {
        return new ImmutablePackedSwitchPayload(getSmaliElements());
    }
}
