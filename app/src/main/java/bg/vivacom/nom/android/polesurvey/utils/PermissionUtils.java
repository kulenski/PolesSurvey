package bg.vivacom.nom.android.polesurvey.utils;

import android.content.pm.PackageManager;

public class PermissionUtils {

    public static boolean verifyPermission(int[] grantsResult) {
        if (grantsResult.length < 1) {
            return false;
        }

        for (int grant: grantsResult) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
