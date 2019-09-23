package com.sap.bookshop.service;

public class ServiceBrokerDto {
	private String clientId;
	private String clientSecretId;
	private String serviceBrokerURL;
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the clientSecretId
	 */
	public String getClientSecretId() {
		return clientSecretId;
	}
	/**
	 * @param clientSecretId the clientSecretId to set
	 */
	public void setClientSecretId(String clientSecretId) {
		this.clientSecretId = clientSecretId;
	}
	/**
	 * @return the serviceBrokerURL
	 */
	public String getServiceBrokerURL() {
		StringBuilder url=new StringBuilder(this.serviceBrokerURL);
		url.append("/oauth/token");
		return url.toString();
	}
	/**
	 * @param serviceBrokerURL the serviceBrokerURL to set
	 */
	public void setServiceBrokerURL(String serviceBrokerURL) {
		this.serviceBrokerURL = serviceBrokerURL;
	}
}

