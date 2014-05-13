/*
 * Copyright (C) 2010-2014 The MPDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namelessdev.mpdroid.locale;

import com.namelessdev.mpdroid.MPDApplication;
import com.namelessdev.mpdroid.NotificationService;
import com.namelessdev.mpdroid.helpers.MPDControl;

import org.a0z.mpd.MPD;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ActionFireReceiver extends BroadcastReceiver {

    private static final String TAG = "MPDroid Locale Plugin";

    private final MPDApplication app = MPDApplication.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getBundleExtra(LocaleConstants.EXTRA_BUNDLE);
        if (bundle == null) {
            return;
        }
        final String action = bundle.getString(EditActivity.BUNDLE_ACTION_STRING);

        switch (action) {
            case NotificationService.ACTION_OPEN_NOTIFICATION:
                final Intent i = new Intent(context, NotificationService.class);
                i.setAction(NotificationService.ACTION_OPEN_NOTIFICATION);
                context.startService(i);
                break;
            case NotificationService.ACTION_CLOSE_NOTIFICATION:
                final Intent i2 = new Intent(context, NotificationService.class);
                i2.setAction(NotificationService.ACTION_CLOSE_NOTIFICATION);
                context.startService(i2);
                break;
            default:
                final MPD mpd = connectToMPD(context);
                int volume = MPDControl.INVALID_INT;

                if (MPDControl.ACTION_SET_VOLUME.equals(action)) {
                    final String volumeString = bundle
                            .getString(EditActivity.BUNDLE_ACTION_EXTRA);
                    if (volumeString != null) {
                        try {
                            volume = Integer.parseInt(volumeString);
                        } catch (final NumberFormatException e) {
                            Log.e(TAG, "Invalid volume string : " + volumeString, e);
                        }
                    }
                }
                MPDControl.run(mpd, action, volume);
                closeMPDConnection();
                break;
        }
    }

    private MPD connectToMPD(Context context) {
        app.init(context);
        app.addConnectionLock(this);
        app.connect();
        return app.oMPDAsyncHelper.oMPD;
    }

    private void closeMPDConnection() {
        app.removeConnectionLock(this);
        // Seems required since I can't exit the dedicated process properly yet
        System.exit(0);
    }
}
