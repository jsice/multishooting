package jsice.network.multishooting.common.net;

import java.io.Serializable;
/**
 * Wiwadh Chinanuphandh
 * 5810400051
 */
public enum ServerMessageType implements Serializable {
    GameStart, MapInfo, UpdateYourObject, UpdateObject, YouDead, NoLocation, DuplicateNameExists, YouKill, TopScore
}
