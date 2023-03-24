package Message;

public enum MessageType {
    Join,
    Read,
    Write,
    ReadQuorum,
    WriteQuorum,
    ReplicasUpdate,
    OK,
    KO
}
