package al.nya.verify.transform.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityRendererTransformer {
    /**
     *
     * @param bytes
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] EntityRenderer.renderWorldPass Name
     * [1] GlStateManager ClassName
     * [2] GlStateManager.alphaFunc Name
     * [3] EntityRenderer.getMouseOver Name (If Margeles Anti Cheat detected send "" )
     * [4] EntityRenderer ClassName
     * [5] EntityRenderer.pointedEntity Name
     * [6] Entity ClassName
     * [7] PlayerControllerMP ClassName
     * [8] PlayerControllerMP.extendedReach
     */
    public static byte[] transform(byte[] bytes,String[] extraData) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0]) && method.desc.equals("(IFJ)V")){
                System.out.println("transform renderWorldPass");
                InsnList render3D = new InsnList();
                InsnList insnList = new InsnList();
                //GETSTATIC al/nya/reflect/Reflect.Instance : Lal/nya/reflect/Reflect;
                //GETFIELD al/nya/reflect/Reflect.eventBus : Lal/nya/reflect/events/EventBus;
                //NEW al/nya/reflect/events/events/EventRender3D
                //DUP
                //FLOAD 1
                //INVOKESPECIAL al/nya/reflect/events/events/EventRender3D.<init> (F)V
                //INVOKEVIRTUAL al/nya/reflect/events/EventBus.callEvent (Lal/nya/reflect/events/events/Event;)V
                render3D.add(new FieldInsnNode(Opcodes.GETSTATIC,"al/nya/reflect/Reflect","Instance","Lal/nya/reflect/Reflect;"));
                render3D.add(new FieldInsnNode(Opcodes.GETFIELD,"al/nya/reflect/Reflect","eventBus","Lal/nya/reflect/events/EventBus;"));
                render3D.add(new TypeInsnNode(Opcodes.NEW,"al/nya/reflect/events/events/EventRender3D"));
                render3D.add(new InsnNode(Opcodes.DUP));
                render3D.add(new VarInsnNode(Opcodes.FLOAD,2));
                render3D.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"al/nya/reflect/events/events/EventRender3D","<init>","(F)V"));
                render3D.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"al/nya/reflect/events/EventBus","callEvent","(Lal/nya/reflect/events/events/Event;)V"));
                boolean transformed = false;
                for (AbstractInsnNode instruction : method.instructions) {
                    insnList.add(instruction);
                    if (instruction instanceof MethodInsnNode && (!transformed)){
                        if (((MethodInsnNode) instruction).owner.equals(extraData[1])
                            && ((MethodInsnNode) instruction).name.equals(extraData[2])&&((MethodInsnNode) instruction).desc.equals("(IF)V")){
                            insnList.add(render3D);
                            transformed = true;
                        }
                    }
                }
                method.instructions = insnList;
                method.maxLocals++;
            }
            if (method.name.equals(extraData[3])&& method.desc.equals("(F)V")){
                System.out.println("transform getMouseOver");
                InsnList insnList = method.instructions;
                MethodInsnNode extendedReach = null;
                for (AbstractInsnNode abstractInsnNode : insnList) {
                    if (abstractInsnNode instanceof MethodInsnNode){
                        if (((MethodInsnNode) abstractInsnNode).name.equals(extraData[8]) && ((MethodInsnNode) abstractInsnNode).owner.equals(extraData[7])
                        && ((MethodInsnNode) abstractInsnNode).desc.equals("()Z")){
                            extendedReach = (MethodInsnNode) abstractInsnNode;
                        }
                    }
                }
                if (extendedReach != null) {
                    AbstractInsnNode jumpInsnNode = extendedReach.getNext();
                    while (jumpInsnNode.getOpcode() != Opcodes.IFLE){
                        jumpInsnNode = jumpInsnNode.getNext();
                    }
                    AbstractInsnNode ICONST = jumpInsnNode.getNext();
                    while (ICONST.getOpcode() != Opcodes.ICONST_1){
                        ICONST = ICONST.getNext();
                    }
                    insnList.remove(ICONST);
                    MethodInsnNode reach = new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","canReach","()Z");
                    insnList.insert(jumpInsnNode,reach);
                    method.instructions = insnList;
                }
            }
        }
        ClassWriter cw = new ClassWriter(cr,0);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
