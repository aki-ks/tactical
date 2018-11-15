package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.conversion.stackasm.AbstractStackInsnWriter;
import me.aki.tactical.conversion.stackasm.StackInsnVisitor;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.constant.PushableConstant;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.AddInsn;
import me.aki.tactical.stack.insn.AndInsn;
import me.aki.tactical.stack.insn.ArrayLengthInsn;
import me.aki.tactical.stack.insn.ArrayLoadInsn;
import me.aki.tactical.stack.insn.ArrayStoreInsn;
import me.aki.tactical.stack.insn.CmpInsn;
import me.aki.tactical.stack.insn.CmpgInsn;
import me.aki.tactical.stack.insn.CmplInsn;
import me.aki.tactical.stack.insn.DivInsn;
import me.aki.tactical.stack.insn.Dup2Insn;
import me.aki.tactical.stack.insn.Dup2X1Insn;
import me.aki.tactical.stack.insn.Dup2X2Insn;
import me.aki.tactical.stack.insn.DupInsn;
import me.aki.tactical.stack.insn.DupX1Insn;
import me.aki.tactical.stack.insn.DupX2Insn;
import me.aki.tactical.stack.insn.FieldGetInsn;
import me.aki.tactical.stack.insn.FieldSetInsn;
import me.aki.tactical.stack.insn.GotoInsn;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.insn.IncrementInsn;
import me.aki.tactical.stack.insn.InstanceOfInsn;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.insn.InvokeInsn;
import me.aki.tactical.stack.insn.LoadInsn;
import me.aki.tactical.stack.insn.ModInsn;
import me.aki.tactical.stack.insn.MonitorEnterInsn;
import me.aki.tactical.stack.insn.MonitorExitInsn;
import me.aki.tactical.stack.insn.MulInsn;
import me.aki.tactical.stack.insn.NegInsn;
import me.aki.tactical.stack.insn.NewArrayInsn;
import me.aki.tactical.stack.insn.NewInsn;
import me.aki.tactical.stack.insn.OrInsn;
import me.aki.tactical.stack.insn.PopInsn;
import me.aki.tactical.stack.insn.PrimitiveCastInsn;
import me.aki.tactical.stack.insn.PushInsn;
import me.aki.tactical.stack.insn.RefCastInsn;
import me.aki.tactical.stack.insn.ReturnInsn;
import me.aki.tactical.stack.insn.ShlInsn;
import me.aki.tactical.stack.insn.ShrInsn;
import me.aki.tactical.stack.insn.StoreInsn;
import me.aki.tactical.stack.insn.SubInsn;
import me.aki.tactical.stack.insn.SwapInsn;
import me.aki.tactical.stack.insn.SwitchInsn;
import me.aki.tactical.stack.insn.ThrowInsn;
import me.aki.tactical.stack.insn.UShrInsn;
import me.aki.tactical.stack.insn.XorInsn;
import me.aki.tactical.stack.invoke.Invoke;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Instruction visitor that collects all events represented as {@link Instruction}.
 */
public class StackInsnWriter extends AbstractStackInsnWriter<LabelNode> {
    private final ConversionContext ctx;

    public StackInsnWriter(ConversionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void registerTargetCell(LabelNode label, Cell<Instruction> cell) {
        ctx.registerForwardInsnCell(label, cell);
    }
}
