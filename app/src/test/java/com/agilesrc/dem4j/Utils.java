/*
 *  Copyright 2001 AgileSrc LLC
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */

package com.agilesrc.dem4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * <p>IConstant - some testing contants</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 */
public final class Utils {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
	public static final String SRC_DIR = "src" + File.separator;
	public static final String TEST_DIR = SRC_DIR + "test" + File.separator;
	public static final String TEST_RESOURCES_DIR = TEST_DIR 
			+ "resources" + File.separator;
	public static final String TARGET = "target" + File.separator;

	//NED files
	public static final File NED_OUTPUT = new File(
			Utils.createDirectory(Utils.TARGET, "ned"));
	
	private static final String _N25_W081_FLT = "floatn25w081_13.flt";
	private static final String _N25_W081_FLT_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N25_W081_FLT;
	
	private static final String _N25_W081_HDR = "floatn25w081_13.hdr";		
	private static final String _N25_W081_HDR_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N25_W081_HDR;
 
	private static final String _N25_W082_FLT = "floatn25w082_13.flt";
	private static final String _N25_W082_FLT_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N25_W082_FLT;
	
	private static final String _N25_W082_HDR = "floatn25w082_13.hdr";
	private static final String _N25_W082_HDR_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N25_W082_HDR;
	
	private static final String _N28_W081_FLT = "floatn28w081_13.flt";
	private static final String _N28_W081_FLT_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N28_W081_FLT;
	
	private static final String _N28_W081_HDR = "floatn28w081_13.hdr";
	private static final String _N28_W081_HDR_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N28_W081_HDR;
 
	private static final String _N29_W082_FLT = "floatn29w082_1.flt";
	private static final String _N29_W082_FLT_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N29_W082_FLT;	
	
	private static final String _N29_W082_HDR = "floatn29w082_1.hdr";		
	private static final String _N29_W082_HDR_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/ned/" + _N29_W082_HDR;

	public static final File N25_W081_FLT_FILE = new File(
			createFilePath(_N25_W081_FLT, NED_OUTPUT.getAbsolutePath()));
	public static final File N25_W081_HDR_FILE = new File(
			createFilePath(_N25_W081_HDR, NED_OUTPUT.getAbsolutePath()));
	public static final File N25_W082_FLT_FILE = new File(
			createFilePath(_N25_W082_FLT, NED_OUTPUT.getAbsolutePath()));
	public static final File N25_W082_HDR_FILE = new File(
			createFilePath(_N25_W082_HDR, NED_OUTPUT.getAbsolutePath()));
	public static final File N28_W081_FLT_FILE = new File(
			createFilePath(_N28_W081_FLT, NED_OUTPUT.getAbsolutePath()));
	public static final File N28_W081_HDR_FILE = new File(
			createFilePath(_N28_W081_HDR, NED_OUTPUT.getAbsolutePath()));
	public static final File N29_W082_FLT_FILE = new File(
			createFilePath(_N29_W082_FLT, NED_OUTPUT.getAbsolutePath()));
	public static final File N29_W082_HDR_FILE = new File(
			createFilePath(_N29_W082_HDR, NED_OUTPUT.getAbsolutePath()));
	
	//SRTM files
	public static final File SRTM3_OUTPUT = new File(
			Utils.createDirectory(Utils.TARGET, "srtm3"));
	
	private static final String _N33E067 = "N33E067.hgt";		
	private static final String _N33E067_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/srtm3/" + _N33E067;

	private static final String _N38W104 = "N38W104.hgt";		
	private static final String _N38W104_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/srtm3/" + _N38W104;
	
	private static final String _N38W105 = "N38W105.hgt";		
	private static final String _N38W105_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/srtm3/" + _N38W105;
	
	public static final File N33E067_FILE = new File(
			createFilePath(_N33E067, SRTM3_OUTPUT.getAbsolutePath()));
	public static final File N38W104_FILE = new File(
			createFilePath(_N38W104, SRTM3_OUTPUT.getAbsolutePath()));
	public static final File N38W105_FILE = new File(
			createFilePath(_N38W105, SRTM3_OUTPUT.getAbsolutePath()));
	
	//DTED files
	public static final File DTED_OUTPUT = new File(
			Utils.createDirectory(Utils.TARGET, "dted"));
	
	private static final String _N30_DT1 = "n30.dt1";		
	private static final String _N30_DT1_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/dted/" + _N30_DT1;

	private static final String _N36_DT0 = "n36.dt0";		
	private static final String _N36_DT0_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/dted/" + _N36_DT0;
	
	private static final String _N40_DT2 = "n40.dt2";		
	private static final String _N40_DT2_URL = 
    		"https://s3.amazonaws.com/dem4j-testing/dted/" + _N40_DT2;
	
	public static final File N30_DT1_FILE = new File(
			createFilePath(_N30_DT1, DTED_OUTPUT.getAbsolutePath()));
	public static final File N36_DT0_FILE = new File(
			createFilePath(_N36_DT0, DTED_OUTPUT.getAbsolutePath()));
	public static final File N40_DT2_FILE = new File(
			createFilePath(_N40_DT2, DTED_OUTPUT.getAbsolutePath()));
	
    //=========================================================================
    // VARIABLES
    //=========================================================================
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
	/**
	 * @throws Exception
	 */
	public static final void downloadTestData() throws Exception {
		//ned
    	downloadTestFile(_N25_W081_FLT_URL, N25_W081_FLT_FILE);
    	downloadTestFile(_N25_W081_HDR_URL, N25_W081_HDR_FILE);
    	downloadTestFile(_N25_W082_FLT_URL, N25_W082_FLT_FILE);
    	downloadTestFile(_N25_W082_HDR_URL, N25_W082_HDR_FILE);
    	downloadTestFile(_N28_W081_FLT_URL, N28_W081_FLT_FILE);
    	downloadTestFile(_N28_W081_HDR_URL, N28_W081_HDR_FILE);
    	downloadTestFile(_N29_W082_FLT_URL, N29_W082_FLT_FILE);
    	downloadTestFile(_N29_W082_HDR_URL, N29_W082_HDR_FILE);
    	
    	//srtm3
    	downloadTestFile(_N33E067_URL, N33E067_FILE);
    	downloadTestFile(_N38W104_URL, N38W104_FILE);
    	downloadTestFile(_N38W105_URL, N38W105_FILE);
    	
    	//dted
    	downloadTestFile(_N30_DT1_URL, N30_DT1_FILE);
    	downloadTestFile(_N36_DT0_URL, N36_DT0_FILE);
    	downloadTestFile(_N40_DT2_URL, N40_DT2_FILE);
	}
	/**
	 * @param downloadUrl
	 * @param output
	 * @param fileName
	 * @throws Exception
	 */
	public static final void downloadTestFile(final String downloadUrl, 
			final File out) throws Exception {
    	URL url = new URL(downloadUrl);

    	if (!out.exists()) {
	    	FileUtils.touch(out);
	    	url.openConnection();
	    	try (InputStream reader = url.openStream();
	    			FileOutputStream writer = new FileOutputStream(out)) {
	    			IOUtils.copy(reader, writer);
	    	}
    	}
	}
	
    /**
	 * Form a file path from a list of path segments without actually creating
	 * anything on disc; so calling #createDirectory("to","some","dir")
	 * would return the string "/to/some/dir/".
	 * 
	 * @param dirs
	 *            one or more path segments (variable length).
	 * @return a string that consists of the named path segments, separated by
	 *         the system dependent file separator character.
	 */
    public static final String createDirectory(final String... dirs) {
        StringBuffer result = new StringBuffer();        
        
        for (final String dir : dirs) {
            result.append(dir + File.separator);
        }
        
        return result.toString();
    }

    /**
	 * Form a file path from a list path segments and a filename without
	 * actually creating anything on disc, so calling
	 * #createFilePath("fileName.txt","to","some","dir") would return the string
	 * "/to/some/dir/fileName.txt".
	 * 
	 * @param fn
	 *            the name of the file (no path)
	 * @param dirs
	 *            one or more directory names (variable length)
	 * 
	 * @return a string that consists of the named path segments ending with the
	 *         named file, all separated by the system dependent file separator character.
	 */
    public static final String createFilePath(final String fn,
        final String... dirs) {
        return createDirectory(dirs) + fn;
    }
    //=========================================================================
    // DEFAULT METHODS
    //=========================================================================

    //=========================================================================
    // PROTECTED METHODS
    //=========================================================================

    //=========================================================================
    // PRIVATE METHODS
    //=========================================================================

    //=========================================================================
    // INNER CLASSES
    //=========================================================================
}
