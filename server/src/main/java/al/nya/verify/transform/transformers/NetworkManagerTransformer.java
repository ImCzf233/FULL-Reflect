package al.nya.verify.transform.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class NetworkManagerTransformer{
    /**
     *
     * @param bytes
     * @param extraData
     * @return
     *
     * ExtraData
     * [0] NetworkManager.channelRead0 Name
     * [1] NetworkManager.sendPacket(Packet) Name
     * [2] Packet ClassName
     */
    public static byte[] transform(byte[] bytes,String[] extraData) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode,0);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(extraData[0]) && method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;L" + extraData[2] + ";)V")){
                InsnList insnList = method.instructions;;
                InsnList recievePacket = new InsnList();
                //ALOAD 2
                //INVOKESTATIC al/nya/reflect/events/EventBus.recievePacketEvent (Ljava/lang/Object;)Z
                //IFEQ L0
                //RETURN
                //L0
                System.out.println("[!] found channelRead0. " + "(Lio/netty/channel/ChannelHandlerContext;L" + extraData[2] + ";)V");
                LabelNode L0 = new LabelNode();
                recievePacket.add(new VarInsnNode(Opcodes.ALOAD,2));
                recievePacket.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","recievePacketEvent","(Ljava/lang/Object;)Z"));
                recievePacket.add(new JumpInsnNode(Opcodes.IFEQ,L0));
                recievePacket.add(new InsnNode(Opcodes.RETURN));
                recievePacket.add(L0);
                //recievePacket.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                insnList.insert(recievePacket);
                method.instructions = insnList;
                method.maxLocals++;
            }
            if (method.name.equals(extraData[1]) && method.desc.equals("(L"+extraData[2]+";)V")){
                InsnList insnList = method.instructions;;
                InsnList sendPacket = new InsnList();
                //ALOAD 1
                //INVOKESTATIC al/nya/reflect/events/EventBus.sendPacketEvent (Ljava/lang/Object;)Z
                //IFEQ L0
                //RETURN
                //L0
                LabelNode L0 = new LabelNode();
                sendPacket.add(new VarInsnNode(Opcodes.ALOAD,1));
                sendPacket.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"al/nya/reflect/events/EventBus","sendPacketEvent","(Ljava/lang/Object;)Z"));
                sendPacket.add(new JumpInsnNode(Opcodes.IFEQ,L0));
                sendPacket.add(new InsnNode(Opcodes.RETURN));
                sendPacket.add(L0);
                //sendPacket.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                insnList.insert(sendPacket);
                method.instructions = insnList;
                method.maxLocals++;
            }
        }
        ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
