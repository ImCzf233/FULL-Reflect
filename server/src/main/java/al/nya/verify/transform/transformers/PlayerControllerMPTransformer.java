package al.nya.verify.transform.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PlayerControllerMPTransformer{
    /**
     *
     * @param bytes
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] PlayerControllerMP.getBlockReachDistance Name
     * [1] PlayerControllerMP.attackEntity Name
     * [2] EntityPlayer ClassName
     * [3] Entity ClassName
     * [4] PlayerControllerMP.extendedReach
     * [5] PlayerControllerMP.windowClick
     */
    public static byte[] transform(byte[] bytes,String[] extraData) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);

        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0]) && method.desc.equals("()F")){
                InsnList reach = new InsnList();
                reach.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","getReachRange","()F"));
                reach.add(new InsnNode(Opcodes.FRETURN));
                method.instructions = reach;
            }
            if (method.name.equals(extraData[1]) &&
                    method.desc.equals("(L"+extraData[2]+";L"+extraData[3]+";)V")){
                InsnList insnList = method.instructions;
                InsnList attack = new InsnList();
                //    ALOAD 1
                //    ALOAD 2
                //    INVOKESTATIC al/nya/reflect/events/EventBus.attack (Lal/nya/reflect/wrapper/wraps/wrapper/entity/EntityPlayer;Lal/nya/reflect/wrapper/wraps/wrapper/entity/Entity;)V
                attack.add(new VarInsnNode(Opcodes.ALOAD,1));
                attack.add(new VarInsnNode(Opcodes.ALOAD,2));
                attack.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus",
                        "attack","(Ljava/lang/Object;Ljava/lang/Object;)V"));
                insnList.insert(attack);
                method.instructions = insnList;
            }
            if (method.name.equals(extraData[5]) &&
                    method.desc.equals("(IIIIL"+extraData[2]+";)V")){
                InsnList insnList = method.instructions;
                InsnList windowClick = new InsnList();
                windowClick.add(new VarInsnNode(Opcodes.ILOAD,1));
                windowClick.add(new VarInsnNode(Opcodes.ILOAD,2));
                windowClick.add(new VarInsnNode(Opcodes.ILOAD,3));
                windowClick.add(new VarInsnNode(Opcodes.ILOAD,4));
                windowClick.add(new VarInsnNode(Opcodes.ALOAD,5));
                windowClick.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","windowClick","(IIIILjava/lang/Object;)V"));
                insnList.insert(windowClick);
                method.instructions = insnList;
            }
        }

        ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
