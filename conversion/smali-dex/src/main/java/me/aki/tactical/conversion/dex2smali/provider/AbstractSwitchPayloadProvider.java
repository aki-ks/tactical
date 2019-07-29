package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.instruction.SwitchPayload;
import org.jf.dexlib2.immutable.instruction.ImmutableSwitchElement;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class AbstractSwitchPayloadProvider<T extends SwitchPayload> implements InstructionProvider<T> {
    private final List<Element> sortedElements;

    public AbstractSwitchPayloadProvider(TreeSet<Integer> sortedKeys, Map<Integer, Instruction> branchTable) {
        this.sortedElements = sortedKeys.stream()
                .map(key -> new Element(key, new OffsetCell(this, branchTable.get(key))))
                .collect(Collectors.toList());
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of();
    }

    @Override
    public List<AbstractOffsetCell> getOffsetCells() {
        return sortedElements.stream()
                .map(Element::getOffsetCell)
                .collect(Collectors.toUnmodifiableList());
    }

    protected List<SwitchElement> getSortedSmaliElements() {
        return sortedElements.stream()
                .map(e -> new ImmutableSwitchElement(e.key, e.offsetCell.get()))
                .collect(Collectors.toList());
    }

    public static class Element {
        private final int key;
        private final AbstractOffsetCell offsetCell;

        Element(int key, AbstractOffsetCell offsetCell) {
            this.key = key;
            this.offsetCell = offsetCell;
        }

        public int getKey() {
            return key;
        }

        public AbstractOffsetCell getOffsetCell() {
            return offsetCell;
        }
    }
}
