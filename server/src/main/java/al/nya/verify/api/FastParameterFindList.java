package al.nya.verify.api;

import java.util.ArrayList;

public class FastParameterFindList extends ArrayList<RequestParameter<?>> {
    public boolean checkNull(){
        for (RequestParameter<?> requestParameter : this) {
           if (requestParameter.getValue() == null) return true;
        }
        return false;
    }
    public RequestParameter<?> getParameter(String name){
        for (RequestParameter<?> requestParameter : this) {
            if (requestParameter.getName().equals(name)) return requestParameter;
        }
        return null;
    }
}
