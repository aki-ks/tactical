package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smali2dex.typing.AmbiguousType;
import me.aki.tactical.conversion.smali2dex.typing.DexTyper;
import me.aki.tactical.conversion.smali2dex.typing.UntypedNumberConstant;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.insn.math.AddInstruction;
import me.aki.tactical.dex.utils.DexCfgGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DexTyperTest {
    @Test
    public void testSimpleOneTypeUse() {
        for (Type type : List.of(IntType.getInstance(), FloatType.getInstance(), ObjectType.OBJECT)) {
            typerTest(1, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.of(type),
                    (method, body) -> {
                        Register number = body.getRegisters().get(0);
                        body.getInstructions().addAll(List.of(
                                new ConstInstruction(constant(0), number),
                                new ReturnInstruction(number)
                        ));
                    }, (method, body) -> {
                        assertEquals(type, body.getRegisters().get(0).getType());
                    }
            );
        }

        for (Type type : List.of(LongType.getInstance(), DoubleType.getInstance())) {
            typerTest(1, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.of(type),
                    (method, body) -> {
                        Register number = body.getRegisters().get(0);
                        body.getInstructions().addAll(List.of(
                                new ConstInstruction(wideConstant(0L), number),
                                new ReturnInstruction(number)
                        ));
                    }, (method, body) -> {
                        assertEquals(type, body.getRegisters().get(0).getType());
                    }
            );
        }
    }

    @Test
    public void testSimpleUseOfMovedValue() {
        for (Type type : List.of(IntType.getInstance(), FloatType.getInstance(), ObjectType.OBJECT)) {
            typerTest(2, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.of(type),
                    (method, body) -> {
                        Register number0 = body.getRegisters().get(0);
                        Register number1 = body.getRegisters().get(1);

                        body.getInstructions().addAll(List.of(
                                new ConstInstruction(constant(0), number1),
                                new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), number1, number0),
                                new ReturnInstruction(number0)
                        ));
                    }, (method, body) -> {
                        Type registerType = body.getRegisters().get(0).getType();
                        if (type instanceof RefType) assertTrue(registerType instanceof RefType);
                        else assertEquals(type, registerType);
                    }
            );
        }
    }

    @Test
    public void dropUselessAmbiguousConstants() {
        typerTest(1, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                (method, body) -> {
                    Register number = body.getRegisters().get(0);
                    body.getInstructions().addAll(List.of(
                            new ConstInstruction(constant(0), number),
                            new ReturnVoidInstruction()
                    ));
                }, (method, body) -> {
                    assertEquals(1, body.getInstructions().size());
                    assertTrue(body.getInstructions().get(0) instanceof ReturnVoidInstruction);
                }
        );
    }

    @Test
    public void dropUselessAmbiguousConstantsAndMoves() {
        typerTest(2, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                (method, body) -> {
                    Register number = body.getRegisters().get(0);
                    Register moveTarget = body.getRegisters().get(1);
                    body.getInstructions().addAll(List.of(
                            new ConstInstruction(constant(0), number),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), number, moveTarget),
                            new ReturnVoidInstruction()
                    ));
                }, (method, body) -> {
                    assertEquals(1, body.getInstructions().size());
                    assertTrue(body.getInstructions().get(0) instanceof ReturnVoidInstruction);
                }
        );
    }

    @Test
    public void dropUselessAmbiguousConstantsAndMultipleMoves() {
        typerTest(4, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                (method, body) -> {
                    Register number = body.getRegisters().get(0);
                    Register moveTarget1 = body.getRegisters().get(1);
                    Register moveTarget2 = body.getRegisters().get(2);
                    Register moveTarget3 = body.getRegisters().get(3);

                    body.getInstructions().addAll(List.of(
                            new ConstInstruction(constant(0), number),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), number, moveTarget1),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), moveTarget1, moveTarget2),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), moveTarget2, moveTarget3),
                            new ReturnVoidInstruction()
                    ));
                }, (method, body) -> {
                    assertEquals(1, body.getInstructions().size());
                    assertTrue(body.getInstructions().get(0) instanceof ReturnVoidInstruction);
                }
        );
    }

    @Test
    public void dropUselessAmbiguousConstantsAndRecursiveMoves() {
        typerTest(4, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                (method, body) -> {
                    Register number = body.getRegisters().get(0);
                    Register moveTarget1 = body.getRegisters().get(1);
                    Register moveTarget2 = body.getRegisters().get(2);
                    Register moveTarget3 = body.getRegisters().get(3);

                    InsertList<Instruction> insns = body.getInstructions();
                    insns.addAll(List.of(
                            new ConstInstruction(constant(0), number),
                    /* 1 */ new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), number, moveTarget1),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), moveTarget1, moveTarget2),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), moveTarget2, moveTarget3),
                            new MoveInstruction(AmbiguousType.IntOrFloatOrRef.getInstance(), moveTarget3, number),
                    /* 5 */ new GotoInstruction(null)
                    ));

                    ((GotoInstruction) insns.get(5)).setTarget(insns.get(1));

                }, (method, body) -> {
                    assertEquals(1, body.getInstructions().size());
                    assertTrue(body.getInstructions().get(0) instanceof GotoInstruction);

                    GotoInstruction gotoInsn = ((GotoInstruction) body.getInstructions().get(0));
                    assertEquals(gotoInsn, gotoInsn.getTarget()); // The goto instruction should not point to one of the removed instructions
                }
        );
    }

    @Test
    public void doNotGetStuckWhileTypingLoops() {
        typerTest(4, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                (method, body) -> {
                    Register number0 = body.getRegisters().get(0);
                    Register number1 = body.getRegisters().get(1);
                    Register number2 = body.getRegisters().get(2);
                    Register number3 = body.getRegisters().get(3);

                    body.getInstructions().addAll(List.of(
                            new ConstInstruction(constant(0), number0),
                            new ConstInstruction(constant(2), number2),
                    /* 2 */ new AddInstruction(IntType.getInstance(), number1, number2, number3),
                    /* 3 */ new GotoInstruction(null)
                    ));

                    ((GotoInstruction) body.getInstructions().get(3)).setTarget(body.getInstructions().get(2));
                }, (method, body) -> {
                    // The test fails with a StackOverflow if it gets stuck in a recursive loop during read/write type propagation
                }
        );
    }

    @Test
    public void testTypingArrayStore() {
        for (Type baseType : List.of(IntType.getInstance(), FloatType.getInstance())) {
            typerTest(4, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                    (method, body) -> {
                        Register size = body.getRegisters().get(0);
                        Register array = body.getRegisters().get(1);
                        Register index = body.getRegisters().get(2);
                        Register value = body.getRegisters().get(3);

                        body.getInstructions().addAll(List.of(
                                new ConstInstruction(constant(4), size),
                                new NewArrayInstruction(new ArrayType(baseType, 1), size, array),
                                new ConstInstruction(constant(0), index),
                                new ConstInstruction(constant(20), value),
                                new ArrayStoreInstruction(AmbiguousType.IntOrFloat.getInstance(), array, index, value),
                                new ReturnVoidInstruction()
                        ));
                    }, (method, body) -> {
                        Register size = body.getRegisters().get(0);
                        Register array = body.getRegisters().get(1);
                        Register index = body.getRegisters().get(2);
                        Register value = body.getRegisters().get(3);

                        assertEquals(IntType.getInstance(), size.getType());
                        assertEquals(array.getType(), new ArrayType(baseType, 1));
                        assertEquals(IntType.getInstance(), index.getType());
                        assertEquals(baseType, value.getType());
                    }
            );
        }
    }

    @Test
    public void testTypingArrayLoad() {
        for (Type baseType : List.of(IntType.getInstance(), FloatType.getInstance())) {
            typerTest(4, false, List.of(IntType.getInstance(), IntType.getInstance()), Optional.empty(),
                    (method, body) -> {
                        Register size = body.getRegisters().get(0);
                        Register array = body.getRegisters().get(1);
                        Register index = body.getRegisters().get(2);
                        Register result = body.getRegisters().get(3);

                        body.getInstructions().addAll(List.of(
                                new ConstInstruction(constant(4), size),
                                new NewArrayInstruction(new ArrayType(baseType, 1), size, array),
                                new ConstInstruction(constant(0), index),
                                new ArrayLoadInstruction(AmbiguousType.IntOrFloat.getInstance(), array, index, result),
                                new ReturnVoidInstruction()
                        ));
                    }, (method, body) -> {
                        Register size = body.getRegisters().get(0);
                        Register array = body.getRegisters().get(1);
                        Register index = body.getRegisters().get(2);
                        Register result = body.getRegisters().get(3);

                        assertEquals(IntType.getInstance(), size.getType());
                        assertEquals(array.getType(), new ArrayType(baseType, 1));
                        assertEquals(IntType.getInstance(), index.getType());
                        assertEquals(baseType, result.getType());
                    }
            );
        }
    }
    private UntypedNumberConstant constant(int i) {
        AmbiguousType type = i == 0 ? AmbiguousType.IntOrFloatOrRef.getInstance() : AmbiguousType.IntOrFloat.getInstance();
        return new UntypedNumberConstant(type, i);
    }

    private UntypedNumberConstant wideConstant(long l) {
        return new UntypedNumberConstant(AmbiguousType.LongOrDouble.getInstance(), l);
    }

    private void typerTest(int registers, boolean hasThisRegister, List<Type> parameterTypes, Optional<Type> returnType, BiConsumer<Method, DexBody> initBody, BiConsumer<Method, DexBody> doAssertions) {
        DexBody body = new DexBody();

        Supplier<Register> newRegister = () -> {
            Register register = new Register(null);
            body.getRegisters().add(register);
            return register;
        };

        for (int i = 0; i < registers; i++) {
            newRegister.get();
        }

        if (hasThisRegister) {
            body.setThisRegister(Optional.of(newRegister.get()));
        }

        for (Type parameterType : parameterTypes) {
            body.getParameterRegisters().add(newRegister.get());
        }

        Method method = new Method("foo", parameterTypes, returnType);
        method.setBody(Optional.of(body));

        initBody.accept(method, body);

        DexTyper typer = new DexTyper(method, new DexCfgGraph(body));
        typer.typeInstructions();
        typer.typeRegisters();

        doAssertions.accept(method, body);
    }
}
