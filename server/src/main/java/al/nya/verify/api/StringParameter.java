package al.nya.verify.api;

public class StringParameter extends RequestParameter<String>{
    public StringParameter(String name) {
        super(name);
    }

    @Override
    public String processData(String v) {
        return v;
    }
}
