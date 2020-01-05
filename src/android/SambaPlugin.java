/**
 * Samba Plugin
 * Copyright (c) 2019, CLOUDSEAT Inc.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * @author AiChen
 * @copyright (c) 2019, CLOUDSEAT Inc.
 * @license https://www.gnu.org/licenses
 * @link https://www.cloudseat.net
 */

package net.cloudseat.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONException;

/**
 * Plugin Main Class
 */
public class SambaPlugin extends CordovaPlugin {

    private static final SambaAdapter samba = new SambaAdapter();

    /**
     * Plugin main method
     */
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callback)
        throws JSONException {
        String path = args.getString(0);

        try {
            switch (action) {
            case "auth":
                String username = args.getString(0);
                String password = args.getString(1);
                samba.setPrincipal(username, password);
                callback.success();
                break;
            case "list":
                list(path, callback);
                break;
            case "read":
                read(path, callback);
                break;
            case "delete":
                samba.delete(path);
                callback.success();
            default:
                callback.error("Undefined method:" + action);
                return false;
            }
            return true;
        } catch (Exception e) {
            callback.error(e.getMessage());
            return false;
        }
    }

    private void list(String path, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.success(samba.list(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void read(String path, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.success(samba.read(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

}
