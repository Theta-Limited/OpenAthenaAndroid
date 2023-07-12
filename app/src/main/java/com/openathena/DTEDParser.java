package com.openathena;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.dted.impl.FileBasedDTED;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

import java.io.File;
import java.io.FileNotFoundException;

public class DTEDParser {
    private FileBasedDTED dted;
    private String filePath;

    public DTEDParser(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("The file " + filePath + " does not exist.");
        }
        try {
            this.dted = new FileBasedDTED(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse the DTED file: " + filePath, e);
        }
    }

    public double getElevation(double lat, double lon) throws Exception {
        Point point = new Point(lat, lon);
        try {
            return this.dted.getElevation(point).getElevation();
        } catch (CorruptTerrainException e) {
            throw new Exception("The terrain data in the DTED file is corrupt.", e);
        } catch (InvalidValueException e) {
            throw new Exception("The provided latitude and longitude values are invalid.", e);
        }
    }

    public String getDTEDLevel() {
        try {
            return this.dted.getDTEDLevel().name();
        } catch (CorruptTerrainException cte) {
            return "CORRUPT";
        }
    }
}
