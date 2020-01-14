/**
 * Samba Adapter
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

import jcifs.smb.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SambaAdapter {

    private static final int BUFFER_SIZE = 8192;
    private NtlmPasswordAuthentication auth;

    /**
     * Get SmbFile instance
     * @param String path
     * @return SmbFile
     */
    public SmbFile getSmbFileInstance(String path) throws IOException {
        return new SmbFile(path, auth);
    }

    /**
     * Lists remote directories and files
     * @param String path
     * @return JSONArray
     */
    public JSONArray listFiles(String path) throws MalformedURLException, SmbException, JSONException {
        SmbFile file = new SmbFile(path, auth);
        if (file.exists()) {
            SmbFile[] files = file.listFiles();
            List<JSONObject> list = parseToList(files);
            Collections.sort(list, new SambaComparator());
            return new JSONArray(list);
        }
        return null;
    }

    /**
     * Sets username and password authentication
     * @param String username
     * @param String password
     * @return
     */
    public void setPrincipal(String username, String password) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            auth = new NtlmPasswordAuthentication(null, username, password);
        } else {
            auth = null;
        }
    }

    /**
     * Creates empty directory
     * @param String path
     * @return JSONObject
     */
    public JSONObject mkdir(String path) throws MalformedURLException, SmbException, JSONException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.mkdir();

        JSONObject entry = new JSONObject();
        entry.put("name", parseName(smbFile.getName()));
        entry.put("type", parseType(smbFile));
        entry.put("path", smbFile.getPath());
        entry.put("size", 0);
        entry.put("lastModified", System.currentTimeMillis());
        return entry;
    }

    /**
     * Creates empty file
     * @param String path
     * @return JSONObject
     */
    public JSONObject mkfile(String path) throws MalformedURLException, SmbException, JSONException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.createNewFile();

        JSONObject entry = new JSONObject();
        entry.put("name", parseName(smbFile.getName()));
        entry.put("type", parseType(smbFile));
        entry.put("path", smbFile.getPath());
        entry.put("size", 0);
        entry.put("lastModified", System.currentTimeMillis());
        return entry;
    }

    /**
     * Deletes directories or files
     * @param String path
     * @return
     */
    public void delete(String path) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.delete();
    }

    /**
     * Renames directory or file
     * @param String path
     * @param String newPath
     * @return
     */
    public void renameTo(String path, String newPath) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.renameTo(new SmbFile(newPath, auth));
    }

    /**
     * Copies directory or file to another path
     * @param String path
     * @param String newPath
     * @return
     */
    public void copyTo(String path, String newPath) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.copyTo(new SmbFile(newPath, auth));
    }

    /**
     * Moves directory or file to another panth
     * @param String path
     * @param String newPath
     * @return
     */
    public void moveTo(String path, String newPath) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.copyTo(new SmbFile(newPath, auth));
        smbFile.delete();
    }

    /**
     * Reads remote file as byte array
     * @param String path
     * @return byte[]
     */
    public byte[] readAsByteArray(String path) throws IOException {
        SmbFile file = new SmbFile(path, auth);
        InputStream in = file.getInputStream();

        byte[] bytes = new byte[(int) file.length()];
        in.read(bytes);
        in.close();
        return bytes;
    }

    /**
     * Reads remote file as string text
     * @param path
     * @return String
     */
    public String readAsText(String path) throws IOException {
        byte[] bytes = readAsByteArray(path);
        return new String(bytes, "UTF-8");
    }

    /**
     * Upload local file to remote
     * @param String localPath
     * @param String smbPath
     * @param Callback callback(progress)
     * @return JSONObject
     */
    public JSONObject upload(String localPath, String smbPath, Callback callback)
        throws IOException, JSONException {

        File file = new File(localPath);
        SmbFile smbFile = new SmbFile(smbPath, auth);
        FileInputStream in = new FileInputStream(file);
        OutputStream out = smbFile.getOutputStream();

        long totalSize = file.length();
        long size = 0;
        byte[] b = new byte[BUFFER_SIZE];
        int len = 0;
        while((len = in.read(b)) > 0) {
            out.write(b, 0, len);
            size += len;
            callback.onProgress((float) size / totalSize);
        }
        in.close();
        out.close();

        JSONObject entry = new JSONObject();
        entry.put("name", parseName(smbFile.getName()));
        entry.put("type", parseType(smbFile));
        entry.put("path", smbFile.getPath());
        entry.put("size", smbFile.length());
        entry.put("lastModified", System.currentTimeMillis());
        return entry;
    }

    /**
     * Download remote file to local path
     * @param String smbPath
     * @param String localPath
     * @return
     */
    public void download(String smbPath, String localPath) throws IOException {
        SmbFile smbFile = new SmbFile(smbPath, auth);
        InputStream in = smbFile.getInputStream();
        FileOutputStream out = new FileOutputStream(localPath);

        byte[] b = new byte[BUFFER_SIZE];
        int len = 0;
        while((len = in.read(b)) > 0) {
            out.write(b, 0, len);
        }
        in.close();
        out.close();
    }

    /* ----------------------------------------------------
     * Private methods
     * ------------------------------------------------- */

    /**
     * Parses smbfiles to arraylist
     * @param SmbFile[] files
     * @return List<JSONObject>
     */
    private List<JSONObject> parseToList(SmbFile[] files) throws SmbException, JSONException {
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (SmbFile file : files) {
            int type = file.getType();
            if (type != SmbFile.TYPE_FILESYSTEM && type != SmbFile.TYPE_SHARE) {
                continue;
            }
            JSONObject entry = new JSONObject();
            entry.put("name", parseName(file.getName()));
            entry.put("type", parseType(file));
            entry.put("path", file.getPath());
            entry.put("size", file.length());
            entry.put("lastModified", file.getLastModified());
            list.add(entry);
        }
        return list;
    }

    /**
     * Trims the '/' in the end.
     * @param String name
     * @return String
     */
    private String parseName(String name) {
        return name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
    }

    /**
     * Gets default type excerpt file
     * @param SmbFile file
     * @return int 0-file, 1-directory, 4-server, 5-share
     */
    private int parseType(SmbFile file) throws SmbException {
        return file.isFile() ? 0 : file.getType();
    }

    /**
     * Private comparator class for sorting
     */
    private class SambaComparator implements Comparator<JSONObject> {
        // Use chinese collator
        private Collator collator = Collator.getInstance(Locale.CHINESE);

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            try {
                int t1 = o1.getInt("type");
                int t2 = o2.getInt("type");
                if (t1 != t2) return t2 - t1;
                return collator.compare(o1.getString("name"), o2.getString("name"));
            } catch (JSONException e) {
                return 0;
            }
        }
    }

}

interface Callback {
    public void onProgress(float progress);
}
