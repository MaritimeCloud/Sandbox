/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.tomcat;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SampleTomcatApplication {

	private static Log logger = LogFactory.getLog(SampleTomcatApplication.class);

	@Bean
	protected ServletContextListener listener() {
		return new ServletContextListener() {

			@Override
			public void contextInitialized(ServletContextEvent sce) {
				logger.info("ServletContext initialized");
			}

			@Override
			public void contextDestroyed(ServletContextEvent sce) {
				logger.info("ServletContext destroyed");
			}

		};
	}

	/** returns the file path to the given resource */
	public static String getResourcePath(String resource) throws URISyntaxException {
		return SampleTomcatApplication.class.getResource("/" + resource).toExternalForm().substring("file:".length());
	}


	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleTomcatApplication.class, args);


		//System.setProperty("javax.net.debug", "all");
		try {
			MmsClientConfiguration conf = MmsClientConfiguration.create("mmsi:1000")
					.setHost("wss://localhost:43235")
					.setKeystore(getResourcePath("client-keystore.jks"))
					//.setKeystore("mc-mms-tls/mc-mms-tls-client-tomcat/src/main/resources/client-keystore.jks")
					.setKeystorePassword("changeit")
					.setTruststore(getResourcePath("client-truststore.jks"))
					//.setTruststore("mc-mms-tls/mc-mms-tls-client-tomcat/src/main/resources/client-truststore.jks")
					.setTruststorePassword("changeit")
                    ;
			MmsClient mmsClient = conf.build();
            mmsClient.connection().awaitConnected(1000, TimeUnit.MILLISECONDS);
			logger.info("MMS Client connected");

            mmsClient.broadcast(new TestBroadcastMessage("Message", 0)).relayed().join();
            logger.info("Broadcast complete");

            mmsClient.shutdown();
            mmsClient.awaitTermination(2, TimeUnit.SECONDS);
            logger.info("Terminated the MMS client " + mmsClient.getClientId());

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
