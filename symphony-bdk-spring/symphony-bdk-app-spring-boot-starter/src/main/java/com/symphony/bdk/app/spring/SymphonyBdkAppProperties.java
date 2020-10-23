package com.symphony.bdk.app.spring;

import com.symphony.bdk.app.spring.properties.AppAuthProperties;
import com.symphony.bdk.app.spring.properties.CorsProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * Configuration Properties for the Symphony BDK Extension App.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "bdk-app")
public class SymphonyBdkAppProperties {

  /**
   * Circle of Trust controller properties.
   */
  private AppAuthProperties auth = new AppAuthProperties();

  /**
   * CORS support properties
   */
  private Map<String, CorsProperties> cors = Collections.emptyMap();
}
