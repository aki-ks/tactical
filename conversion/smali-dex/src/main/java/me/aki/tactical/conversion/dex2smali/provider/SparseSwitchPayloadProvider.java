package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload;
import org.jf.dexlib2.immutable.instruction.ImmutableSparseSwitchPayload;

import java.util.Map;
import java.util.TreeSet;

public class SparseSwitchPayloadProvider extends AbstractSwitchPayloadProvider<SparseSwitchPayload> {
    public SparseSwitchPayloadProvider(TreeSet<Integer> sortedKeys, Map<Integer, Instruction> branchTable) {
        super(sortedKeys, branchTable);
    }

    @Override
    public Format getFormat() {
        return Format.SparseSwitchPayload;
    }

    @Override
    public SparseSwitchPayload newInstance() {
        return new ImmutableSparseSwitchPayload(getSortedSmaliElements());
    }
}
