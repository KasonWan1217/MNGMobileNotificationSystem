package util;

public class DBEnumValue {
    public enum AppName {
        BEA_APP_Group
    }
    public enum ArnType {
        Topic,
        Platform
    }
    public enum TargetType {
        Group,
        Personal
    }
    public enum Status {
        Success,
        Fail,
        Reset
    }
    public enum Action {
        Subscribe,
        Unsubscribe
    }
}
