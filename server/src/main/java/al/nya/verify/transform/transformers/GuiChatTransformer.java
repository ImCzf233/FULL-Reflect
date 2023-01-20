package al.nya.verify.transform.transformers;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiChatTransformer {
    /**
     *
     * @param classfileBuffer
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] GuiScreen.keyType Name
     * [1] GuiScreen.onGuiClosed Name
     */
    public static byte[] transform(byte[] classfileBuffer,String[] extraData) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {

        }
        ClassWriter cw = new ClassWriter(cr,0);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
