/**
 * Copyright (c) 2014 OGN, All Rights Reserved.
 */

package org.ogn.client.demo;

import static java.lang.System.out;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import org.ogn.client.AircraftBeaconListener;
import org.ogn.client.OgnClient;
import org.ogn.client.OgnClientFactory;
import org.ogn.commons.beacon.AircraftBeacon;
import org.ogn.commons.beacon.AircraftDescriptor;
import org.ogn.commons.igc.IgcLogger;
import org.ogn.commons.utils.JsonUtils;

/**
 * A small demo program demonstrating the basic usage of the ogn-client.
 * 
 * @author wbuczak
 */
public class OgnDemoAircraftBeaconsClient {

	static {
		// aprs filter can be passed either by a jvm env variable or directly in the connect() method - see below
		// System.setProperty(OgnClientProperties.PROP_OGN_CLIENT_APRS_FILTER, "r/+49.782/+19.450/5");
	}

	static IgcLogger	igcLogger	= new IgcLogger();

	// enable if you want to log to IGC files
	static boolean		logIGC		= false;

	static class AcListener implements AircraftBeaconListener {

		void postData(String data) {

			StringEntity entity = new StringEntity(data,
					ContentType.APPLICATION_JSON);

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost("https://www.gliding.com.au/members2/flarm");
			request.setEntity(entity);

			HttpResponse response = null;
			try {
				response = httpClient.execute(request);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(response.getStatusLine().getStatusCode());
		}

		@Override
		public void onUpdate(AircraftBeacon beacon, Optional<AircraftDescriptor> descriptor) {

			// print the beacon
			String data = JsonUtils.toJson(beacon);
			out.println(data);
			postData(data);

			// if the aircraft has been recognized print its descriptor too
			if (descriptor.isPresent()) {
				out.println(JsonUtils.toJson(descriptor.get()));
			}

			if (logIGC)
				igcLogger.log(beacon, descriptor);
		}
	}

	public static void main(String[] args) throws Exception {
		final OgnClient client = OgnClientFactory.createClient();

		System.out.println("connecting3...");

		client.connect("r/-34.641667/+148.025/20"); // Coota
//		client.connect("r/-33.369504/+149.521638/20"); // Piper's Airfield - Bathurst Soaring Club
//		client.connect("r/-34.038078/+150.686759/20"); // Camden

		//client.connect();

		client.subscribeToAircraftBeacons(new AcListener());

		Thread.sleep(Long.MAX_VALUE);
	}

}