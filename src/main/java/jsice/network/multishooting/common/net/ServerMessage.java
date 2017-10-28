package jsice.network.multishooting.common.net;

import java.io.Serializable;

public class ServerMessage implements Serializable {
    private ServerMessageType type;
    private int statusCode;
    private Serializable data;

    public ServerMessage(ServerMessageType type, int statusCode) {
        this.type = type;
        this.statusCode = statusCode;
    }

    public ServerMessageType getType() {
        return type;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return statusCode + " " + type;
    }
}
