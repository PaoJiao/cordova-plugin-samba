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

package net.cloudseat.cordova;

import jcifs.smb.*;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

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

    /**
     * Password authentication
     */
    private NtlmPasswordAuthentication auth;

    /**
     * Lists remote directories and files
     */
    public JSONArray list(String path) throws MalformedURLException, SmbException, JSONException {
        SmbFile file = new SmbFile(path, auth);
        if (file.exists()) {
            SmbFile[] files = file.listFiles();
            // Parse smb array to arraylist
            List<JSONObject> list = parseToList(files);
            // Sort arraylist by locale
            Collections.sort(list, new SambaComparator());
            return new JSONArray(list);
        }
        return null;
    }

    /**
     * Sets username and password authentication
     */
    public void setPrincipal(String username, String password) {
        auth = new NtlmPasswordAuthentication(null, username, password);
    }

    /**
     * Creates empty directory
     */
    public void mkdir(String path) throws MalformedURLException, SmbException {
        SmbFile smb = new SmbFile(path, auth);
        smb.mkdir();
    }

    /**
     * Creates empty file
     */
    public void mkfile(String path) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.createNewFile();
    }

    /**
     * Deletes directories or files
     */
    public void delete(String path) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.delete();
    }

    /**
     * Renames directory or file
     */
    public void rename(String path, String newPath) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.renameTo(new SmbFile(newPath, auth));
    }

    /**
     * Copies directory or file to another panth
     */
    public void copy(String path, String newPath) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.copyTo(new SmbFile(newPath, auth));
    }

    /**
     * Moves directory or file to another panth
     */
    public void move(String path, String newPath) throws MalformedURLException, SmbException {
        SmbFile smbFile = new SmbFile(path, auth);
        smbFile.copyTo(new SmbFile(newPath, auth));
        smbFile.delete();
    }

    /**
     * Reads the content of remote file
     */
    public byte[] read(String path) throws IOException {
        SmbFile file = new SmbFile(path, auth);
        InputStream is = file.getInputStream();

        byte[] bytes = new byte[(int) file.length()];
        is.read(bytes);
        is.close();
        return bytes;
    }

    /**
     * Download remote file to local path
     */
    public void download(String path, String localPath) throws IOException {
        SmbFile file = new SmbFile(path, auth);
        InputStream in = file.getInputStream();
        FileOutputStream out = new FileOutputStream(localPath);

        byte[] b = new byte[8192];
        int len, total = 0;
        while((len = in.read(b)) > 0) {
            out.write(b, 0, len);
            total += len;
            System.out.println(total + " bytes transfered");
        }

        in.close();
        out.close();
    }

    /* ----------------------------------------------------
     * Private methods
     * ------------------------------------------------- */

    /**
     * Parses smbfiles to arraylist
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
            entry.put("path", file.getPath());
            entry.put("size", file.length());
            entry.put("lastModified", file.getLastModified());
            entry.put("isDirectory", file.isDirectory());
            list.add(entry);
        }
        return list;
    }

    /**
     * Trims the '/' in the end.
     */
    private String parseName(String name) {
        return name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
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
                boolean d1 = o1.getBoolean("isDirectory");
                boolean d2 = o2.getBoolean("isDirectory");
                if (d1 && !d2) return -1;
                if (!d1 && d2) return 1;
                return collator.compare(o1.getString("name"), o2.getString("name"));
            } catch (JSONException e) {
                return 0;
            }
        }
    }

}
