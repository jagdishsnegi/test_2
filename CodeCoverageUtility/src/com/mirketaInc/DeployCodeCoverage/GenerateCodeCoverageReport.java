package com.mirketaInc.DeployCodeCoverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mirketaInc.Utilities.PropertyReader;
import com.sforce.soap.metadata.CodeCoverageResult;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DeployDetails;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestSuccess;
import com.sforce.soap.metadata.RunTestsResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;

public class GenerateCodeCoverageReport {
	final static Logger logger = Logger.getLogger(GenerateCodeCoverageReport.class);
	static MetadataConnection metadataConnection;
	public static Map<String, RunTestResultInfo> codeCoverageInfoMap = new HashMap<String, RunTestResultInfo>();
	public static ArrayList<RunTestResultInfo> testFailureResult = new ArrayList<RunTestResultInfo>();
	public static ArrayList<RunTestResultInfo> testSuccessResult = new ArrayList<RunTestResultInfo>();
	public static String codeCoverageIssues;
	public static String requestId;
	public static DeployStatus deployStatus;
	public static String sfUserName;
	public static String sfPassword;
	public static String salesforceEnvironmentName;
	static Integer numOfTests;
	static Integer numOfTestsFailed;
	static String sfEndPoint;
	static float individualCodeCoverage;
	static float overallCodeCoverage;
	static boolean codeCoverageAlertsRequired = false;
	static boolean isTestFailed = false;
	static Double totalTimeTakenToRunTests;
	static int totalNumOfLocations = 0;
	static int totalNumOfLocationsNotCovered = 0;

	public static void main(String[] arg) {
		logger.info("[GenerateCodeCoverageReport.main]:starts");
		if (arg.length > 2) {
			if (arg[0] != null) {
				sfUserName = arg[0];
			} else {
				logger.warn("[RunDataLogger.Main]: Salesforce user name expected as parameter");
			}
			if (arg[1] != null) {
				sfPassword = arg[1];
			} else {
				logger.warn("[RunDataLogger.Main]: Salesforce password expected as parameter");
			}
			if (arg[2] != null) {
				sfEndPoint = arg[2];
			} else {
				logger.warn("[RunDataLogger.Main]: Salesforce endPoint expected as parameter");
			}
		} else {
			logger.warn("No arguments found in the parameters or incorrect number of arguments!");
			logger.info("[GenerateCodeCoverageReport.main]:ends");
			return;
		}

		getRequestId();
		if (requestId != null && !requestId.isEmpty()) {
			if (requestId.length() == 15 || requestId.length() == 18) {
				metadataLogin();
				if (metadataConnection != null) {
					getRunTestResultInfo();
					if (!codeCoverageInfoMap.isEmpty() || !testFailureResult.isEmpty() || !testSuccessResult.isEmpty())
						createCodeCoverageReport();
					else
						logger.warn(
								"No information about the success or failure of the associated deploy call.Hence, no code coverage report generated");
				} else {
					logger.warn("Metadata Connection required");
				}
			} else {
				logger.warn("ID should be 15 or 18-character long!");
			}
		} else {
			logger.warn(
					"Deployment(or Request) ID not generated of the associated deploy call.Hence, no code coverage report generated");
		}

		logger.info("[GenerateCodeCoverageReport.main]:ends");
	}

	public static void getRequestId() {
		logger.info("[GenerateCodeCoverageReport.getRequestId]:starts");
		try {

			File f = new File(PropertyReader.deployLogFileLocation);
			BufferedReader brd = new BufferedReader(new FileReader(f));
			String metaString = "";
			Pattern pattern = Pattern.compile("[a-zA-Z0-9]{15,18}");
			while ((metaString = brd.readLine()) != null) {
				if (metaString.contains("Request ID")) {
					Matcher matcher = pattern.matcher(metaString);
					while (matcher.find()) {
						requestId = matcher.group();
						break;
					}
				}
			}
			brd.close();

		} catch (Exception e) {
			logger.error("GenerateCodeCoverageReport.Exception:" + e);
		}
		logger.info("[GenerateCodeCoverageReport.getRequestId]:ends");
	}

	public static void metadataLogin() {
		logger.info("[GenerateCodeCoverageReport.metadataLogin]:starts");
		try {
			final ConnectorConfig partnerConfig = new ConnectorConfig();
			partnerConfig.setServiceEndpoint(sfEndPoint + "/services/Soap/u/" + PropertyReader.APIVersion);
			partnerConfig.setAuthEndpoint(sfEndPoint + "/services/Soap/u/" + PropertyReader.APIVersion);
			partnerConfig.setManualLogin(true);
			LoginResult loginResult = new PartnerConnection(partnerConfig).login(sfUserName, sfPassword);
			final ConnectorConfig config = new ConnectorConfig();
			config.setServiceEndpoint(loginResult.getMetadataServerUrl());
			config.setSessionId(loginResult.getSessionId());
			metadataConnection = new MetadataConnection(config);
		} catch (Exception e) {
			logger.error("GenerateCodeCoverageReport.Exception:" + e);
		}
		logger.info("[GenerateCodeCoverageReport.metadataLogin]:ends");
	}

	public static void getRunTestResultInfo() {
		logger.info("[GenerateCodeCoverageReport.getRunTestResultInfo]:starts");
		try {
			StringBuffer sb = new StringBuffer();
			DeployResult deployResult = metadataConnection.checkDeployStatus(requestId, true);
			deployStatus = deployResult.getStatus();
			DeployDetails deployDetails = deployResult.getDetails();
			if (deployDetails != null) {
				RunTestsResult rtr = deployDetails.getRunTestResult();
				numOfTests = rtr.getNumTestsRun();
				numOfTestsFailed = rtr.getNumFailures();
				totalTimeTakenToRunTests = rtr.getTotalTime();
				System.out.print (rtr.getCodeCoverage());
				if (rtr.getCodeCoverage() != null) {
					for (CodeCoverageResult ccr : rtr.getCodeCoverage()) {
						RunTestResultInfo runTestResultInfo = new RunTestResultInfo();
						String key = ccr.getNamespace() + "," + ccr.getName();
						runTestResultInfo.setNamespace(ccr.getNamespace());
						runTestResultInfo.setName(ccr.getName());
						runTestResultInfo.setNumOfLocation(ccr.getNumLocations());
						totalNumOfLocations += ccr.getNumLocations();
						runTestResultInfo.setNumOfLocationNotCovered(ccr.getNumLocationsNotCovered());
						totalNumOfLocationsNotCovered += ccr.getNumLocationsNotCovered();
						if (ccr.getNumLocations() != 0) {
							runTestResultInfo.setIndividualCodeCoverage(
									(1 - ((float) ccr.getNumLocationsNotCovered() / ccr.getNumLocations())) * 100);
						}
						if (runTestResultInfo.getIndividualCodeCoverage() < Float
								.parseFloat(PropertyReader.individualCodeCoverageAlertPercentage)) {
							codeCoverageAlertsRequired = true;
						}
						codeCoverageInfoMap.put(key, runTestResultInfo);
					}
					overallCodeCoverage = (1 - ((float) totalNumOfLocationsNotCovered / totalNumOfLocations)) * 100;
					if (overallCodeCoverage < Float.parseFloat(PropertyReader.totalCodeCoverageAlertPercentage)) {
						codeCoverageAlertsRequired = true;

					}
				}
				if (rtr.getCodeCoverageWarnings() != null) {
					sb.delete(0, sb.length());
					for (CodeCoverageWarning ccw : rtr.getCodeCoverageWarnings()) {
						codeCoverageAlertsRequired=true;
						RunTestResultInfo runTestResultInfo = new RunTestResultInfo();
						String key = ccw.getNamespace() + "," + ccw.getName();
						if (ccw.getName() != null) {
							if (codeCoverageInfoMap.containsKey(key)) {
								codeCoverageInfoMap.get(key).setCodeCoverageWarnings(ccw.getMessage());
							} else {
								runTestResultInfo.setNamespace(ccw.getNamespace());
								runTestResultInfo.setName(ccw.getName());
								codeCoverageInfoMap.put(key, runTestResultInfo);
							}
						} else {
							sb.append(ccw.getMessage() + ";");
							codeCoverageAlertsRequired=true;
						}

					}
					codeCoverageIssues = sb.toString();
				}
				if (rtr.getFailures() != null) {
					for (RunTestFailure rtf : rtr.getFailures()) {
						isTestFailed = true;
						System.out.println("failed");
						RunTestResultInfo runTestResultInfo = new RunTestResultInfo();
						runTestResultInfo.setName(rtf.getName());
						runTestResultInfo.setNamespace(rtf.getNamespace());
						runTestResultInfo.setMethodFailed(rtf.getMethodName());
						runTestResultInfo.setFailureMessage(rtf.getMessage());
						runTestResultInfo.setStackTraceForFailure(rtf.getStackTrace());
						runTestResultInfo.setTimeTakenToRunTest(rtf.getTime());
						testFailureResult.add(runTestResultInfo);

					}
				}
				if (rtr.getSuccesses() != null) {
					for (RunTestSuccess rts : rtr.getSuccesses()) {
						RunTestResultInfo runTestResultInfo = new RunTestResultInfo();
						runTestResultInfo.setName(rts.getName());
						runTestResultInfo.setNamespace(rts.getNamespace());
						runTestResultInfo.setMethodSucceeded(rts.getMethodName());
						runTestResultInfo.setTimeTakenToRunTest(rts.getTime());
						testSuccessResult.add(runTestResultInfo);
					}
				}
			}
		} catch (Exception e) {
			logger.error("GenerateCodeCoverageReport.Exception:" + e);
		}
		logger.info("[GenerateCodeCoverageReport.getRunTestResultInfo]:ends");
	}

	public static void createCodeCoverageReport() {
		logger.info("[GenerateCodeCoverageReport.createCodeCoverageReport]:starts");
		try {

			XSSFWorkbook workbook = new XSSFWorkbook();
			System.out.print(isTestFailed);
			System.out.println(codeCoverageAlertsRequired);
			if (isTestFailed || codeCoverageAlertsRequired) {
				if (!codeCoverageInfoMap.isEmpty()) {
					XSSFSheet sheet = workbook.createSheet("Code Coverage");
					int countRow = 0;
					int countCell = -1;
					XSSFRow row = sheet.createRow(countRow);
					String[] headerTestFailure = new String[] { "Namespace", "Name", "Num Of Locations",
							"Num Of Locations Not covered", "Code Coverage", "Code Coverage Warnings" };
					for (String header : headerTestFailure) {
						Cell cell = row.createCell(++countCell);
						cell.setCellValue(header);
					}
					for (String key : codeCoverageInfoMap.keySet()) {
						RunTestResultInfo runTestResultInfo = codeCoverageInfoMap.get(key);
						countCell = -1;
						countRow++;
						XSSFRow valueRow = sheet.createRow(countRow);
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getNamespace());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getName());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getNumOfLocation());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getNumOfLocationNotCovered());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getIndividualCodeCoverage());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getCodeCoverageWarnings());

					}

					XSSFRow valueRow = sheet.createRow(++countRow);
					valueRow.createCell(2).setCellValue("Total no. of Locations:" + totalNumOfLocations);
					valueRow.createCell(3)
							.setCellValue("Total no. of Locations Not Covered:" + totalNumOfLocationsNotCovered);
					valueRow.createCell(4).setCellValue("Overall Code Coverage:" + overallCodeCoverage);

					valueRow = sheet.createRow(++countRow);
					valueRow.createCell(0).setCellValue("Code Coverage Warnings:" + codeCoverageIssues);
					valueRow = sheet.createRow(++countRow);
					valueRow.createCell(0).setCellValue("Total time taken:" + totalTimeTakenToRunTests + " ms");
				} else {

				}
				if (!testFailureResult.isEmpty()) {
					XSSFSheet sheet = workbook.createSheet("Test Failure");
					int countRow = 0;
					int countCell = -1;
					XSSFRow row = sheet.createRow(countRow);
					String[] headerTestFailure = new String[] { "Namespace", "Name", "Method Failed", "Stack Trace",
							"Failure Message", "Time (in ms)" };
					for (String header : headerTestFailure) {
						Cell cell = row.createCell(++countCell);
						cell.setCellValue(header);
					}
					for (RunTestResultInfo runTestResultInfo : testFailureResult) {

						countCell = -1;
						countRow++;
						XSSFRow valueRow = sheet.createRow(countRow);
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getNamespace());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getName());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getMethodFailed());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getStackTraceForFailure());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getFailureMessage());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getTimeTakenToRunTest());

					}

					XSSFRow valueRow = sheet.createRow(++countRow);
					valueRow.createCell(0).setCellValue("No. of tests:" + numOfTests);
					valueRow = sheet.createRow(++countRow);
					valueRow.createCell(0).setCellValue("No. of tests failed:" + numOfTestsFailed);
				}
				if (!testSuccessResult.isEmpty()) {
					XSSFSheet sheet = workbook.createSheet("Test Success");
					int countRow = 0;
					int countCell = -1;
					XSSFRow row = sheet.createRow(countRow);
					String[] headerTestFailure = new String[] { "Namespace", "Name", "Method Succeeded",
							"Time (in ms)" };
					for (String header : headerTestFailure) {
						Cell cell = row.createCell(++countCell);
						cell.setCellValue(header);
					}
					for (RunTestResultInfo runTestResultInfo : testSuccessResult) {

						countCell = -1;
						countRow++;
						XSSFRow valueRow = sheet.createRow(countRow);
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getNamespace());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getName());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getMethodSucceeded());
						valueRow.createCell(++countCell).setCellValue(runTestResultInfo.getTimeTakenToRunTest());
					}

					XSSFRow valueRow = sheet.createRow(++countRow);
					valueRow.createCell(0).setCellValue("No. of tests:" + numOfTests);
					valueRow = sheet.createRow(++countRow);
					valueRow.createCell(0).setCellValue("No. of tests Succeeded:" + (numOfTests - numOfTestsFailed));
				}

				FileOutputStream out = new FileOutputStream(new File(PropertyReader.reportPath + File.separator
						+ "Report_" + requestId + "_" + deployStatus + ".xlsx"));
				workbook.write(out);
				out.close();
				workbook.close();
			}
			else {
				logger.info("No test failure or code coverage warnings detected.Hence report not generated!");
			}
		} catch (Exception e) {
			logger.error("GenerateCodeCoverageReport.Exception:" + e);
		}
		logger.info("[GenerateCodeCoverageReport.createCodeCoverageReport]:ends");
	}

}
