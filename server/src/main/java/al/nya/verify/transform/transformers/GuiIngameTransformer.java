package al.nya.verify.transform.transformers;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiIngameTransformer{
    /**
     *
     * @param classfileBuffer
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] GuiIngameForge.renderTooltip Name
     * [1] ScaledResolution ClassName
     */
    public static byte[] transform(byte[] classfileBuffer,String[] extraData) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0]) &&
                    method.desc.equals("(L"+extraData[1]+";F)V")){
                InsnList render2DInsn = new InsnList();
                render2DInsn.add(new FieldInsnNode(Opcodes.GETSTATIC,"al/nya/reflect/Reflect","Instance","Lal/nya/reflect/Reflect;"));
                render2DInsn.add(new FieldInsnNode(Opcodes.GETFIELD,"al/nya/reflect/Reflect","eventBus","Lal/nya/reflect/events/EventBus;"));
                render2DInsn.add(new TypeInsnNode(Opcodes.NEW,"al/nya/reflect/events/events/EventRender2D"));
                render2DInsn.add(new InsnNode(Opcodes.DUP));
                render2DInsn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"al/nya/reflect/events/events/EventRender2D","<init>","()V"));
                render2DInsn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"al/nya/reflect/events/EventBus","callEvent","(Lal/nya/reflect/events/events/Event;)V"));
                InsnList returnList = new InsnList();
                returnList.add(new InsnNode(Opcodes.RETURN));
                method.instructions.insert(render2DInsn);
                break;
            }
        }
        ClassWriter cw = new ClassWriter(cr,0);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
