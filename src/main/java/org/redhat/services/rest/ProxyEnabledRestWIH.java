package org.redhat.services.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyEnabledRestWIH extends RESTWorkItemHandler {
	private static final Logger logger = LoggerFactory.getLogger(ProxyEnabledRestWIH.class);

	@Override
	public HttpClient getHttpClient(Integer readTimeout, Integer connectTimeout) {
		if (getDoCacheClient() && HTTP_CLIENT_API_43) {
			if (cachedClient == null) {
				cachedClient = getNewPooledHttpClient(readTimeout, connectTimeout);
			}

			return cachedClient;
		}

		if (HTTP_CLIENT_API_43) {
			RequestConfig config = RequestConfig.custom().setSocketTimeout(readTimeout)
					.setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout).build();

			HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(config);
			clientBuilder.useSystemProperties(); // enable proxy

			HttpClient httpClient = clientBuilder.build();
			logger.info("Creating proxy enabled http client");

			return httpClient;
		} else {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);
			httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);

			return httpClient;
		}

	}

	@Override
	protected CloseableHttpClient getNewPooledHttpClient(Integer readTimeout, Integer connectTimeout) {

		RequestConfig config = RequestConfig.custom().setSocketTimeout(readTimeout).setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout).build();

		return HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(config)
				.useSystemProperties().build(); //proxy
	}

}
