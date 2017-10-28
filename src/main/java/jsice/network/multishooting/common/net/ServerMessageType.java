package jsice.network.multishooting.common.net;

import java.io.Serializable;

public enum ServerMessageType implements Serializable {
    GameStart, UpdateYourObject, UpdateObject, YouDead, NoLocation, DuplicateNameExists, YouKill
}
