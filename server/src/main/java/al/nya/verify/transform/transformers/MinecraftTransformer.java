package al.nya.verify.transform.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
public class MinecraftTransformer{
    /**
     *
     * @param classfileBuffer
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] Minecraft.runTick Name
     * [1] Minecraft.dispatchKeypresses Name
     * [2] Minecraft.runGameLoop Name
     * [3] Minecraft.loadWorld Name
     * [4] WorldClient ClassName
     */
    public static byte[] transform(byte[] classfileBuffer,String[] extraData) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0]) && method.desc.equals("()V")){
                //OnUpdate
                InsnList insnList = method.instructions;
                InsnList runTick = new InsnList();
                runTick.add(new FieldInsnNode(Opcodes.GETSTATIC,"al/nya/reflect/Reflect","Instance","Lal/nya/reflect/Reflect;"));
                runTick.add(new FieldInsnNode(Opcodes.GETFIELD,"al/nya/reflect/Reflect","eventBus","Lal/nya/reflect/events/EventBus;"));
                runTick.add(new TypeInsnNode(Opcodes.NEW,"al/nya/reflect/events/events/EventTick"));
                runTick.add(new InsnNode(Opcodes.DUP));
                runTick.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"al/nya/reflect/events/events/EventTick","<init>","()V"));
                runTick.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"al/nya/reflect/events/EventBus","callEvent","(Lal/nya/reflect/events/events/Event;)V"));
                insnList.insert(runTick);
                method.instructions = insnList;
            }
            if (method.name.equals(extraData[1])&& method.desc.equals("()V")){
                InsnList insnList = method.instructions;
                InsnList eventKey = new InsnList();
                eventKey.add(new FieldInsnNode(Opcodes.GETSTATIC,"al/nya/reflect/Reflect","Instance","Lal/nya/reflect/Reflect;"));
                eventKey.add(new FieldInsnNode(Opcodes.GETFIELD,"al/nya/reflect/Reflect","eventBus","Lal/nya/reflect/events/EventBus;"));
                eventKey.add(new TypeInsnNode(Opcodes.NEW,"al/nya/reflect/events/events/EventKey"));
                eventKey.add(new InsnNode(Opcodes.DUP));
                eventKey.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"al/nya/reflect/events/events/EventKey","<init>","()V"));
                eventKey.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"al/nya/reflect/events/EventBus","callEvent","(Lal/nya/reflect/events/events/Event;)V"));
                insnList.insert(eventKey);
                method.instructions = insnList;
            }
            if(method.name.equals(extraData[2])&& method.desc.equals("()V")){
                InsnList insnList = method.instructions;
                InsnList eventLoop = new InsnList();
                eventLoop.add(new FieldInsnNode(Opcodes.GETSTATIC,"al/nya/reflect/Reflect","Instance","Lal/nya/reflect/Reflect;"));
                eventLoop.add(new FieldInsnNode(Opcodes.GETFIELD,"al/nya/reflect/Reflect","eventBus","Lal/nya/reflect/events/EventBus;"));
                eventLoop.add(new TypeInsnNode(Opcodes.NEW,"al/nya/reflect/events/events/EventLoop"));
                eventLoop.add(new InsnNode(Opcodes.DUP));
                eventLoop.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"al/nya/reflect/events/events/EventLoop","<init>","()V"));
                eventLoop.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"al/nya/reflect/events/EventBus","callEvent","(Lal/nya/reflect/events/events/Event;)V"));
                insnList.insert(eventLoop);
                method.instructions = insnList;
            }
            if(method.name.equals(extraData[3])&& method.desc.equals("(L"+extraData[4]+";)V")){
                InsnList insnList = method.instructions;
                InsnList loadWorld = new InsnList();
                loadWorld.add(new VarInsnNode(Opcodes.ALOAD,1));
                loadWorld.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","loadWorld","(Ljava/lang/Object;)V"));
                insnList.insert(loadWorld);
                method.instructions = insnList;
            }
        }
        ClassWriter cw = new ClassWriter(cr,0);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
