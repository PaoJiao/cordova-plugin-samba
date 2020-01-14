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

package net.cloudseat.smbova;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import jcifs.smb.SmbFile;
import com.greatape.bmds.BufferedMediaDataSource;

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

        switch (action) {
            case "auth":
                String username = args.getString(0);
                String password = args.getString(1);
                samba.setPrincipal(username, password);
                callback.success();
                break;
            case "listFiles": listFiles(args, callback); break;
            case "readAsText": readAsText(args, callback); break;
            case "readAsByteArray": readAsByteArray(args, callback); break;
            case "upload": upload(args, callback); break;
            case "mkfile": mkfile(args, callback); break;
            case "mkdir": mkdir(args, callback); break;
            case "delete": delete(args, callback); break;
            case "openMedia": openMedia(args, callback); break;
            case "openFile": openFile(args, callback); break;
            case "wakeOnLan": wakeOnLan(args, callback); break;
            default:
                callback.error("Undefined method:" + action);
                return false;
        }
        return true;
    }

    private void listFiles(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    callback.success(samba.listFiles(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void readAsText(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    callback.success(samba.readAsText(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void readAsByteArray(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    callback.success(samba.readAsByteArray(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void mkfile(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    callback.success(samba.mkfile(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void mkdir(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    callback.success(samba.mkdir(path));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void delete(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    samba.delete(path);
                    callback.success();
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void upload(CordovaArgs args, CallbackContext callback) throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String localPath = args.getString(0);
                    String smbPath = args.getString(1);

                    Context context = cordova.getActivity().getApplicationContext();
                    String nativePath = NativePath.parse(context, localPath);

                    int index = nativePath.lastIndexOf("/");
                    String fileName = nativePath.substring(index + 1);

                    JSONObject result = samba.upload(nativePath, smbPath + fileName, new Callback() {
                        @Override
                        public void onProgress(float progress) {
                            webView.sendJavascript("window.samba.onUpload(" + progress + ")");
                        }
                    });
                    callback.success(result);
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void openFile(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(Uri.parse(path), mimeType);
                    cordova.getActivity().startActivity(intent);

                    callback.success(mimeType);
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void openMedia(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = args.getString(0);
                    MediaPlayerActivity.dataSource = createBufferedMediaDataSource(path);

                    Intent intent = new Intent(cordova.getActivity(), MediaPlayerActivity.class);
                    cordova.getActivity().startActivity(intent);
                    callback.success();
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void wakeOnLan(CordovaArgs args, CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String mac = args.getString(0);
                    WakeOnLan.broadcast(mac);
                    callback.success();
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private BufferedMediaDataSource createBufferedMediaDataSource(String path) throws IOException {
        SmbFile smbFile = samba.getSmbFileInstance(path);

        return new BufferedMediaDataSource(new BufferedMediaDataSource.StreamCreator() {
            @Override
            public InputStream openStream() throws IOException {
                return smbFile.getInputStream();
            }
            @Override
            public long length() throws IOException {
                return smbFile.length();
            }
            @Override
            public String typeName() {
                return "SmbFile";
            }
        });
    }

}
