package jsice.network.multishooting.common.net;

import java.io.Serializable;

public enum ServerMessageType implements Serializable {
    GameStart, MapInfo, UpdateYourObject, UpdateObject, YouDead, NoLocation, DuplicateNameExists, YouKill, TopScore
}
