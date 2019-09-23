package com.sap.bookshop.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

@Component
public class VCAPUtil {

	/**
	 * Logger for logging all the information.
	 */
	Logger logger = LoggerFactory.getLogger(getClass());

	private String clientId;

	private String clientSecretId;

	private String featureFlagUserId;

	private String featureFlagSecretId;

	private Map<String, ServiceBrokerDto> serviceBrokerMap = new HashMap<>();
	/** service broker names */
	private List<String> serviceBrokerNames = new ArrayList<>();
	
	/**
	 * This constructor is used to set the credentials.
	 * 
	 * @param environment
	 *            the spring core environment
	 */
	public VCAPUtil(Environment environment) {

		logger.info("Reading VCAP properties: ");
		/** only one instance of UAA wil be binded */

		/** patter to match the name */
		String pattern = "^vcap.services.(?<name>.*).label$";
		Pattern regex = Pattern.compile(pattern);
		MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
		AtomicReference<String> uaaServiceName = new AtomicReference<>();
		AtomicReference<String> featureFlagSrvName = new AtomicReference<>();
		StreamSupport.stream(propertySources.spliterator(), false)
				.filter(propertySource -> propertySource instanceof MapPropertySource)
				.flatMap(propertySource -> ((MapPropertySource) propertySource).getSource().entrySet().stream())
				.forEach(entry -> {
					String key = entry.getKey();
					Matcher matcher = regex.matcher(key);
					if (matcher.find()) {
						String serviceName = matcher.group("name");
						String value =  entry.getValue().toString();
						if (value.equals("xsuaa")) {
							uaaServiceName.set(serviceName);

						}
						/** added for feature flags */
						if (value.equals("feature-flags")) {
							featureFlagSrvName.set(serviceName);
						}
						if(serviceName !=null){
						/** add only the service broker service details */
						List<String> tags = environment.getProperty("vcap.services." + serviceName + ".tags",
								ArrayList.class);
						if(tags != null){
						/**for testing*/
						logger.info("the tags");
						tags.forEach(v -> logger.info(v));
						if (tags.contains("sales")) {
							/** add to the service broker names list */
							serviceBrokerNames.add(serviceName);
						}
						}
						}

					}

				});
		StringBuilder clientIdBuilder = new StringBuilder("vcap.services.");
		clientIdBuilder.append(uaaServiceName).append(".credentials.clientid");
		StringBuilder clientSecretBuilder = new StringBuilder("vcap.services.");
		clientSecretBuilder.append(uaaServiceName).append(".credentials.clientsecret");
		StringBuilder featureFlagIdBuilder = new StringBuilder("vcap.services.");
		featureFlagIdBuilder.append(featureFlagSrvName).append(".credentials.username");
		StringBuilder featureFlagSecretBuilder = new StringBuilder("vcap.services.");
		featureFlagSecretBuilder.append(featureFlagSrvName).append(".credentials.password");
		/** get the client id and client secret id and set it */
		this.clientId = environment.getProperty(clientIdBuilder.toString(), "");
		this.clientSecretId = environment.getProperty(clientSecretBuilder.toString(), "");
		this.featureFlagSecretId = environment.getProperty(featureFlagSecretBuilder.toString(), "");
		this.featureFlagUserId = environment.getProperty(featureFlagIdBuilder.toString(), "");
		serviceBrokerNames.forEach(broker -> {
			StringBuilder clientIdBldr = new StringBuilder("vcap.services.");
			clientIdBldr.append(broker).append(".credentials.uaa.clientid");
			StringBuilder clientSecret = new StringBuilder("vcap.services.");
			clientSecret.append(broker).append(".credentials.uaa.clientsecret");
			StringBuilder urlBuilder = new StringBuilder("vcap.services.");
			urlBuilder.append(broker).append(".credentials.uaa.url");
			String brokerClientId = environment.getProperty(clientIdBldr.toString(), "");
			String brokerSecretId = environment.getProperty(clientSecret.toString(), "");
			String url = environment.getProperty(urlBuilder.toString(), "");
			ServiceBrokerDto dto = new ServiceBrokerDto();
			dto.setClientId(brokerClientId);
			dto.setClientSecretId(brokerSecretId);
			dto.setServiceBrokerURL(url);
			serviceBrokerMap.put(broker, dto);

		});

	}
	
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @return the clientSecretId
	 */
	public String getClientSecretId() {
		return clientSecretId;
	}

	/**
	 * @return the featureFlagUserId
	 */
	public String getFeatureFlagUserId() {
		return featureFlagUserId;
	}

	/**
	 * @return the featureFlagSecretId
	 */
	public String getFeatureFlagSecretId() {
		return featureFlagSecretId;
	}

	/**
	 * @return the serviceBrokerMap
	 */
	public Map<String, ServiceBrokerDto> getServiceBrokerMap() {
		return serviceBrokerMap;
	}

	/**
	 * @param serviceBrokerMap
	 *            the serviceBrokerMap to set
	 */
	public void setServiceBrokerMap(Map<String, ServiceBrokerDto> serviceBrokerMap) {
		this.serviceBrokerMap = serviceBrokerMap;
	}

	/**
	 * @return the serviceBrokerNames
	 */
	public List<String> getServiceBrokerNames() {
		return serviceBrokerNames;
	}

	/**
	 * @param serviceBrokerNames
	 *            the serviceBrokerNames to set
	 */
	public void setServiceBrokerNames(List<String> serviceBrokerNames) {
		this.serviceBrokerNames = serviceBrokerNames;
	}

}
