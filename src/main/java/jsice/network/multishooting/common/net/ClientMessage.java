package jsice.network.multishooting.common.net;

import java.io.Serializable;

public class ClientMessage implements Serializable {

    private final ClientMessageType type;
    private String detail;
    private Serializable data;

    public ClientMessage(ClientMessageType type, Serializable data) {
        this.type = type;
        this.data = data;
    }

    public ClientMessageType getType() {
        return type;
    }

    public Serializable getData() {
        return data;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return type.toString() + " " + data.toString();
    }
}
