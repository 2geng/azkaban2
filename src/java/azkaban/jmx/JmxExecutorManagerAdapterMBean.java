package azkaban.jmx;

import java.util.List;

public interface JmxExecutorManagerAdapterMBean {
	@DisplayName("OPERATION: getNumRunningFlows")
	public int getNumRunningFlows();
	
	@DisplayName("OPERATION: getRunningFlows")
	public String getRunningFlows();
	
	@DisplayName("OPERATION: getUpdaterThreadStage")
	public String getUpdaterThreadStage();
	
	@DisplayName("OPERATION: getExecutorThreadState")
	public String getExecutorManagerThreadState();

	@DisplayName("OPERATION: isThreadActive")
	public boolean isExecutorManagerThreadActive();

	@DisplayName("OPERATION: getLastThreadCheckTime")
	public Long getLastExecutorManagerThreadCheckTime();

	@DisplayName("OPERATION: getPrimaryExecutorHostPorts")
	public List<String> getPrimaryExecutorHostPorts();
	
}
