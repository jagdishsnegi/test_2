package com.mirketaInc.Utilities;

import java.io.FileReader;
import java.util.Properties;


public class PropertyReader {
	
	public static String APIVersion;
	public static String deployLogFileLocation;
	public static String reportPath;
	public static String individualCodeCoverageAlertPercentage;
	public static String totalCodeCoverageAlertPercentage;
	public static Properties pObj = new Properties();
	static {
		FileReader fileRdr;
		try {
			fileRdr = new FileReader("config.properties");
			pObj.load(fileRdr);
			APIVersion=pObj.getProperty("API_VERSION").trim();
			deployLogFileLocation = pObj.getProperty("deployLogFileLocation").trim();
			reportPath=pObj.getProperty("ReportPath").trim();
			individualCodeCoverageAlertPercentage=pObj.getProperty("IndividualCodeCoverageAlertPercentage").trim();
			totalCodeCoverageAlertPercentage=pObj.getProperty("TotalCodeCoverageAlertPercentage").trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
