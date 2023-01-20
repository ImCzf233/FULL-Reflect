package al.nya.verify.transform.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EntityLivingBaseTransformer{
    /**
     * ExtraData
     * [0] EntityPlayerSP.jump
     */
    public static byte[] transform(byte[] bytes,String[] extraData) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0]) && method.desc.equals("()V")){
                InsnList insnList = method.instructions;
                InsnList jump = new InsnList();
                jump.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","jump","()V"));
                insnList.insert(jump);
                method.instructions = insnList;
            }
        }
        ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
