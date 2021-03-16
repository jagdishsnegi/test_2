package com.mirketaInc.DeployCodeCoverage;



public class RunTestResultInfo {
	public String namespace;
	public String name;
	public int numOfLocation;
	public int numOfLocationNotCovered;
	public float individualCodeCoverage;
	public String codeCoverageWarnings;
	public String methodSucceeded;
	public String methodFailed;
	public String stackTraceForFailure;
	public String failureMessage;
	public double timeTakenToRunTest;
	
	//setters
	public void setNamespace(String namespace) {
		this.namespace=namespace;
	}
	public void setName(String name) {
		this.name=name;
	}
	public void setNumOfLocation(int i) {
		this.numOfLocation=i;
	}
	public void setNumOfLocationNotCovered(int i) {
		this.numOfLocationNotCovered=i;
	}
	public void setIndividualCodeCoverage(float individualCodeCoverage) {
		this.individualCodeCoverage=individualCodeCoverage;
	}
	public void setCodeCoverageWarnings(String codeCoverageWarnings) {
		this.codeCoverageWarnings=codeCoverageWarnings;
	}
	public void setMethodSucceeded(String methodSucceeded) {
		this.methodSucceeded=methodSucceeded;
	}
	public void setMethodFailed(String methodFailed) {
		this.methodFailed=methodFailed;
	}
	public void setStackTraceForFailure(String stackTraceForFailure) {
		this.stackTraceForFailure=stackTraceForFailure;
	}
	public void setFailureMessage(String failureMessage) {
		this.failureMessage=failureMessage;
	}
	public void setTimeTakenToRunTest(double timeTakenToRunTest) {
		this.timeTakenToRunTest=timeTakenToRunTest;
	}
	
	//getters
	public String getNamespace() {
		return namespace;
	}
	public String getName() {
		return name;
	}
	public int getNumOfLocation() {
		return numOfLocation;
	}
	public int getNumOfLocationNotCovered() {
		return numOfLocationNotCovered;
	}
	public double getIndividualCodeCoverage() {
		return individualCodeCoverage;
	}
	public String getCodeCoverageWarnings() {
		return codeCoverageWarnings;
	}
	public String getMethodSucceeded() {
		return methodSucceeded;
	}
	public String getMethodFailed() {
		return methodFailed;
	}
	public String getStackTraceForFailure() {
		return stackTraceForFailure;
	}
	public String getFailureMessage() {
		return failureMessage;
	}
	public Double getTimeTakenToRunTest() {
		return timeTakenToRunTest;
	}
}
