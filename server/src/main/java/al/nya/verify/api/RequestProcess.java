package al.nya.verify.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequestProcess {
    public static FastParameterFindList process(String request , List<? extends RequestParameter<?>> parameters){
        FastParameterFindList list = new FastParameterFindList();
        list.addAll(parameters);
        String decodedUrl;
        try {
            decodedUrl = URLDecoder.decode(request, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return list;
        }
        String[] args = decodedUrl.split("&");
        for (RequestParameter<?> requestParameter : list) {
            for (String arg : args) {
                if (arg.contains(requestParameter.getName()+"=")){
                    String value = arg.replaceFirst(requestParameter.getName()+"=","");
                    requestParameter.process(value);
                }
            }
        }
        return list;
    }
}
