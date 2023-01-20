package al.nya.verify.transform.transformers;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import static al.nya.verify.transform.NameManager.*;
public class EntityPlayerSPTransformer{
    /**
     *
     * @param classfileBuffer
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] EntityPlayerSP.onUpdate Name
     * [1] EntityPlayerSP ClassName
     * [2] EntityPlayerSP.onUpdateWalkingPlayer Name
     * [3] EntityPlayerSP.onLivingUpdate Name
     * [4] EntityPlayerSP.sendChatMessage Name
     * [5] EntityPlayerSP.sendChatMessage Signature
     * [6] Entity.isRiding Name
     */
    public static byte[] transform(byte[] classfileBuffer,String[] extraData) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[3]) && method.desc.equals("()V")){
                //OnUpdate
                InsnList insnList = method.instructions;
                InsnList eventUpdatePost = new InsnList();
                eventUpdatePost.add(new FieldInsnNode(Opcodes.GETSTATIC,"al/nya/reflect/Reflect","Instance","Lal/nya/reflect/Reflect;"));
                eventUpdatePost.add(new FieldInsnNode(Opcodes.GETFIELD,"al/nya/reflect/Reflect","eventBus","Lal/nya/reflect/events/EventBus;"));
                eventUpdatePost.add(new TypeInsnNode(Opcodes.NEW,"al/nya/reflect/events/events/EventUpdate"));
                eventUpdatePost.add(new InsnNode(Opcodes.DUP));
                eventUpdatePost.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"al/nya/reflect/events/events/EventUpdate","<init>","()V"));
                eventUpdatePost.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"al/nya/reflect/events/EventBus","callEvent","(Lal/nya/reflect/events/events/Event;)V"));
                insnList.insert(eventUpdatePost);
                method.instructions = insnList;
            }
            if (method.name.equals(extraData[3]) && method.desc.equals("()V")){
                //OnUpdate
                InsnList insnList = method.instructions;
                InsnList noslow = new InsnList();
                for (AbstractInsnNode abstractInsnNode : insnList) {
                    //INVOKEVIRTUAL net/minecraft/client/entity/EntityPlayerSP.func_71039_bw ()Z
                    if (abstractInsnNode instanceof MethodInsnNode){
                        if (abstractInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode) abstractInsnNode).owner.equals(extraData[1]) && ((MethodInsnNode) abstractInsnNode).name.equals(extraData[6])&&
                        ((MethodInsnNode) abstractInsnNode).desc.equals("()Z")){
                            AbstractInsnNode L12 = abstractInsnNode.getNext();
                            if (L12 instanceof JumpInsnNode){
                                noslow.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","isNoSlow","()Z"));
                                noslow.add(new JumpInsnNode(Opcodes.IFNE, ((JumpInsnNode) L12).label));
                                System.out.println("Insert");
                                insnList.insert(L12.getNext(),noslow);
                            }
                            break;
                        }
                    }
                }
                method.instructions = insnList;
            }
            if (method.name.equals(extraData[0]) && method.desc.equals("()V")) {
                InsnList insnList = method.instructions;
                InsnList preUpdate = new InsnList();
                //INVOKESTATIC al/nya/reflect/events/EventBus.preUpdate ()V
                preUpdate.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus", "preUpdate", "()V"));
                InsnList postUpdate = new InsnList();
                postUpdate.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus", "postUpdate", "()V"));
                for (AbstractInsnNode abstractInsnNode : insnList) {
                    if (abstractInsnNode instanceof MethodInsnNode){
                        if (abstractInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && ((MethodInsnNode) abstractInsnNode).owner.equals(extraData[1])
                        && ((MethodInsnNode) abstractInsnNode).name.equals(extraData[2])
                        && ((MethodInsnNode) abstractInsnNode).desc.equals("()V")){
                            insnList.insertBefore(abstractInsnNode,preUpdate);
                            insnList.insert(abstractInsnNode,postUpdate);
                        }
                    }
                }
                method.instructions = insnList;
            }
            if (method.name.equals(extraData[4]) && method.desc.equals(extraData[5])){
                InsnList insnListOld = method.instructions;
                InsnList insnList = new InsnList();
                InsnList chat = new InsnList();
                //ALOAD 1
                //INVOKESTATIC al/nya/reflect/modules/Command.message (Ljava/lang/String;)Z
                //IFEQ L0
                //GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
                //LDC ""
                //INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
                //        L0
                //FRAME SAME
                //RETURN
                LabelNode L0 = new LabelNode();
                chat.add(new VarInsnNode(Opcodes.ALOAD,1));
                chat.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "al/nya/reflect/events/EventBus","onMessage","(Ljava/lang/String;)Z"));
                chat.add(new JumpInsnNode(Opcodes.IFEQ,L0));
                insnList.add(chat);
                insnList.add(insnListOld);
                insnList.add(L0);
                insnList.add(new InsnNode(Opcodes.RETURN));
                method.instructions = insnList;
                method.maxLocals ++;
            }
        }
        ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
