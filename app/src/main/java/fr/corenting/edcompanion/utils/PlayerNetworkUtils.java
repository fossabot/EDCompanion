package fr.corenting.edcompanion.utils;


import android.content.Context;

import fr.corenting.edcompanion.R;
import fr.corenting.edcompanion.network.player.EDSMPlayer;
import fr.corenting.edcompanion.network.player.FrontierPlayer;
import fr.corenting.edcompanion.network.player.InaraPlayer;
import fr.corenting.edcompanion.network.player.PlayerNetwork;

public class PlayerNetworkUtils {

    private static final String frontier = "Frontier API (directly with your game account)";
    private static final String edsm = "EDSM";
    private static final String inara = "Inara";

    public static PlayerNetwork getCurrentPlayerNetwork(Context context) {
        String current = SettingsUtils.getString(context, context.getResources().getString(R.string.settings_cmdr_source));

        return getCurrentPlayerNetwork(context, current);
    }

    public static PlayerNetwork getCurrentPlayerNetwork(Context context, String value) {
        if (value.equals("")) {
            return new FrontierPlayer(context);
        }

        switch (value) {
            case edsm:
                return new EDSMPlayer(context);
            case inara:
                return new InaraPlayer(context);
            case frontier:
                return new FrontierPlayer(context);
            default:
                return new InaraPlayer(context);
        }
    }

    public static String[] getSourcesList() {
        return new String[]{frontier, edsm, inara};
    }


    public static boolean setupOk(Context context) {
        String apiKey = SettingsUtils.getString(context, context.getString(R.string.settings_cmdr_password));
        String commanderName = SettingsUtils.getString(context, context.getString(R.string.settings_cmdr_username));
        PlayerNetwork network = getCurrentPlayerNetwork(context);

        if (network.getClass() == FrontierPlayer.class) {
            return !OAuthUtils.getAccessToken(context).equals("");
        } else {
            return !(network.usePassword() && apiKey.equals("")) && !commanderName.equals("");
        }
    }
}
