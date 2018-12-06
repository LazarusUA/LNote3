package com.note.lazarus.lnote;

import android.os.Environment;

import java.io.File;

public class ManagerCode extends Manager {
    private final String SDCARD0 = Environment.getExternalStorageDirectory().toString();
    private final String SDCARD = getSdCardPath();

    String getSize(File file) throws Exception {
        float size = (float) file.length();
        String sizeToString = "";

        if (size < 1_024) {
            sizeToString = String.valueOf((int) size)+" б";
        } else if (size >= 1_024 && size < 1_048_576) {
            size /= 1_024;
            sizeToString = String.format("%.1f", size) + " кб";
        } else if (size >= 1_048_576) {
            size /= 1_048_576;
            sizeToString = String.format("%.1f", size) + " мб";
        }
        return sizeToString;
    }

    String getPathBack(String pathName) {
        int last;
        if (!pathName.equals(SDCARD0) &&
                !pathName.equals(SDCARD)) {
            last = pathName.lastIndexOf("/");
            return pathName.substring(0, last);
        } else
            return SDCARD0;
    }

    long getSpace(File file) {
        long count = 0;
        if (file.isDirectory()) {
            File[] openFile = file.listFiles();
            count = openFile.length;
        }
        return count;
    }

    String getFileType(String name) {
        String type = "0";
        if (name.lastIndexOf(".") != -1) {
            type = (name.substring(name.lastIndexOf(".") + 1, name.length())).toLowerCase();
        }
        return type;
    }

    String getName(String s) {
        String type = getFileType(s);
        return type.equals("0") ? s : s.substring(0,s.length()-type.length()-1);
    }

    void AllDelete(File f) {
        if (f.isFile()) {
            f.delete();
        } else if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                AllDelete(file);
            }
            f.delete();
        }
    }

    String getSdCardPath() {
        String Path = "";

        for (File Af : new File("/storage").listFiles()) {
            if ((Af.listFiles() != null) &&
                    !(Af.getAbsolutePath().equals(SDCARD0))) {
                Path = Af.getAbsolutePath();
            }
        }
        return Path;
    }
}
