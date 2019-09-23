package com.sap.bookshop.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SBHelperUtil {

	@Autowired
	private VCAPUtil lbnvcapUtil;
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * This method is used to give the user token for service broker
	 * @param serviceBrokerName the service broker name
	 * @return the token
	 * @throws LBNTokenException the LBN token exception
	 * 
	 */
	public String getSBToken(String serviceBrokerName){
		ServiceBrokerDto dto=this.getServiceBrokerDto(serviceBrokerName);
		String token=null;

		HttpHeaders headers = new HttpHeaders();
		
		String credentials = dto.getClientId() + ":" + dto.getClientSecretId();
		String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        
        String url = dto.getServiceBrokerURL() + "?grant_type=client_credentials";
		@SuppressWarnings("rawtypes")
		ResponseEntity<HashMap> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, request, HashMap.class);
		token = (String) responseEntity.getBody().get("access_token");

		return token;
	}
	/**
	 * This method is used to get the service broker dto for the given service broker name
	 * @param serviceBrokerName the name
	 * @return the dto
	 */
	private ServiceBrokerDto getServiceBrokerDto(String serviceBrokerName) {
		Map<String, ServiceBrokerDto> serviceBrokerMap=this.getLbnvcapUtil().getServiceBrokerMap();
		if(serviceBrokerMap != null){
			return serviceBrokerMap.get(serviceBrokerName);
		}
		return null;
	}

	/**
	 * @return the lbnvcapUtil
	 */
	public VCAPUtil getLbnvcapUtil() {
		return lbnvcapUtil;
	}

	/**
	 * @param lbnvcapUtil the lbnvcapUtil to set
	 */
	public void setLbnvcapUtil(VCAPUtil lbnvcapUtil) {
		this.lbnvcapUtil = lbnvcapUtil;
	}
	
}
