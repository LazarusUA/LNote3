package com.note.lazarus.lnote;

import java.io.File;
import java.util.Arrays;

public class ManagerList extends Manager {
    File[] Filter(File[] file) throws Exception {
        int k = 0;

        for (File anF : file) {
            if (!anF.isHidden()) k++;
        }

        File[] arF = new File[k];
        k = 0;

        for (File aFile : file) {
            if (!aFile.isHidden()) {
                arF[k] = aFile;
                k++;
            }
        }

        Arrays.sort(arF);

        return arF;
    }
}
