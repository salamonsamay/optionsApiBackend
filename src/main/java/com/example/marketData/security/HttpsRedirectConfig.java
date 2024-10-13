//package com.example.marketData.security;
//
//import org.apache.catalina.connector.Connector;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class HttpsRedirectConfig {
//
//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> redirectToHttps() {
//        return factory -> factory.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
//    }
//
//    private Connector httpToHttpsRedirectConnector() {
//        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//        connector.setScheme("http");
//        connector.setPort(8080);
//        connector.setSecure(false);
//        connector.setRedirectPort(8443);
//        return connector;
//    }
//}
