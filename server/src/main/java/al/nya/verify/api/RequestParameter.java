package al.nya.verify.api;

import lombok.Getter;

public abstract class RequestParameter<T> {
    @Getter private String name;
    @Getter private T value = null;
    public RequestParameter(String name){
        this.name = name;
    }
    public void process(String s){
        value = processData(s);
    }
    public abstract T processData(String v);
}
