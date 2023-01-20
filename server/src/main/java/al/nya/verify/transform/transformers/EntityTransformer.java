package al.nya.verify.transform.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityTransformer{
    /**
     *
     * @param bytes
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] Entity.moveEntity Name
     * [1] Entity.stepHeight Name
     * [2] ProfilerClass Name
     * [3] Profiler.endSection Name
     * [4] Entity ClassName
     */
    public static byte[] transform(byte[] bytes,String[] extraData) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0])&&method.desc.equals("(DDD)V")){
                //OnUpdate
                InsnList insnList = method.instructions;
                InsnList move = new InsnList();
                //ALOAD 0
                //INVOKESTATIC al/nya/reflect/events/EventBus.move (Ljava/lang/Object;)V
                move.add(new VarInsnNode(Opcodes.ALOAD,0));
                move.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus","move","(Ljava/lang/Object;)V"));
                insnList.insert(move);
                method.instructions = insnList;
                method.maxLocals ++;
            }
            if (method.name.equals(extraData[0])&&method.desc.equals("(DDD)V")){
                //OnUpdate
                InsnList insnList = method.instructions;
                InsnList step = new InsnList();
                LabelNode Lnew = new LabelNode();
                step.add(Lnew);
                step.add(new VarInsnNode(Opcodes.ALOAD,0));
                step.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus","postStep","(Ljava/lang/Object;)V"));
                for (AbstractInsnNode abstractInsnNode : insnList) {
                    if (abstractInsnNode instanceof MethodInsnNode){
                        if (((MethodInsnNode) abstractInsnNode).owner.equals(extraData[2]) &&
                        ((MethodInsnNode) abstractInsnNode).name.equals(extraData[3]) && ((MethodInsnNode) abstractInsnNode).desc.equals("()V")){
                            AbstractInsnNode node = abstractInsnNode.getPrevious();
                            while (!(node instanceof LabelNode)){
                                node = node.getPrevious();
                            }
                            LabelNode L33 = (LabelNode) node;
                            step.insert(new JumpInsnNode(Opcodes.GOTO,L33));
                            insnList.insertBefore(L33,step);
                            while (!(node instanceof JumpInsnNode)){
                                node = node.getPrevious();
                            }
                            node = node.getPrevious();
                            while (!(node instanceof JumpInsnNode)){
                                node = node.getPrevious();
                            }
                            JumpInsnNode jumpL33 = (JumpInsnNode) node;
                            jumpL33.label = Lnew;
                            break;
                        }
                    }
                }
                method.instructions = insnList;
            }
            if (method.name.equals(extraData[0])&&method.desc.equals("(DDD)V")){
                //OnUpdate
                InsnList insnList = method.instructions;
                InsnList step = new InsnList();
                step.add(new VarInsnNode(Opcodes.ALOAD,0));
                step.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus","preStep","(Ljava/lang/Object;)V"));
                int index = 0;
                for (AbstractInsnNode abstractInsnNode : insnList) {
                    if (abstractInsnNode instanceof FieldInsnNode){
                        if (((FieldInsnNode) abstractInsnNode).name.equals(extraData[1]) &&
                        ((FieldInsnNode) abstractInsnNode).owner.equals(extraData[4])&&
                        ((FieldInsnNode) abstractInsnNode).desc.equals("F")){
                            index ++;
                            if (index == 2){
                                insnList.insertBefore(abstractInsnNode,step);
                            }
                        }
                    }
                }
                method.instructions = insnList;
            }
        }
        ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
