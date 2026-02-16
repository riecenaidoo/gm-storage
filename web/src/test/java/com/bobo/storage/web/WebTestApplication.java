package com.bobo.storage.web;

import com.bobo.semantic.TestApplication;
import org.springframework.mock.web.MockHttpServletRequest;

@TestApplication
public class WebTestApplication {

  public static MockHttpServletRequest testSchemeAuthority(
      MockHttpServletRequest mockHttpServletRequest) {
    mockHttpServletRequest.setScheme("http");
    mockHttpServletRequest.setServerName("localhost");
    mockHttpServletRequest.setServerPort(8080);
    return mockHttpServletRequest;
  }

  public static String testSchemeAuthority() {
    return String.format("%s://%s:%d", "http", "localhost", 8080);
  }
}
