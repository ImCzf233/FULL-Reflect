package al.nya.verify.transform;

import al.nya.verify.Data.ControlCommand;
import al.nya.verify.Data.irc.CommandTransformClass;
import al.nya.verify.Data.irc.CommandTransformFinish;
import al.nya.verify.Data.irc.CommandUpload;
import al.nya.verify.UserData;
import al.nya.verify.transform.transformers.*;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TransformManager {
    public static void transformClass(UserData ud, CommandTransformClass commandTransformClass){
        byte[] bytes = ud.buffer.array();
        switch (commandTransformClass.transformType){
            case "EntityPlayerSP":
                bytes = EntityPlayerSPTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "EntityRenderer":
                bytes = EntityRendererTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "Entity":
                bytes = EntityTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "GuiIngameForge":
                bytes = GuiIngameForgeTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "GuiIngame":
                bytes = GuiIngameTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "Minecraft":
                bytes = MinecraftTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "NetworkManager":
                bytes = NetworkManagerTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "PlayerControllerMP":
                bytes = PlayerControllerMPTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "GuiChat":
                bytes = GuiChatTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "GuiNewChat":
                bytes = GuiNewChatTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
            case "DoMCer":
                bytes = DoMCerTransformer.transform();
                break;
            case "EntityLivingBase":
                bytes = EntityLivingBaseTransformer.transform(ud.buffer.array(),commandTransformClass.extraData);
                break;
        }
        ud.buffer = ByteBuffer.wrap(bytes);
        ud.getConnect().send(new Gson().toJson(new ControlCommand("UploadData",new Gson().toJson(new CommandUpload(bytes.length)))));
        ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
        try {
            while (bos.available() != 0){
                if (bos.available() > 512){
                    byte[] b = new byte[512];
                    bos.read(b);
                    ud.getConnect().send(b);
                }else {
                    byte[] b = new byte[bos.available()];
                    bos.read(b);
                    ud.getConnect().send(b);
                }
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        ud.getConnect().send(new Gson().toJson(new ControlCommand("TransformFinish",new Gson().toJson(new CommandTransformFinish(commandTransformClass.transformType)))));
    }
}
