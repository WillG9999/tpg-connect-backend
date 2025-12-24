package com.tpg.connect.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

@Slf4j
public class CucumberHooks {

    private static WireMockServer wireMockServer;
    private static final String MOCK_SERVER_BASE_URL_PROPERTY = "mock.server.baseUrl";
    private static final String DEFAULT_HOST = "localhost";

    @BeforeAll
    public static void startWireMock() {
        Optional.ofNullable(wireMockServer)
                .filter(WireMockServer::isRunning)
                .ifPresent(WireMockServer::stop);

        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        configureFor(DEFAULT_HOST, wireMockServer.port());
        String baseUrl = String.format("http://%s:%d", DEFAULT_HOST, wireMockServer.port());
        System.setProperty(MOCK_SERVER_BASE_URL_PROPERTY, baseUrl);

        log.info("WireMock started on:: {}", baseUrl);
    }

    @Before
    public void resetStubs() {
        Optional.ofNullable(wireMockServer)
                .filter(WireMockServer::isRunning)
                .ifPresent(WireMockServer::resetAll);
    }

    @AfterAll
    public static void stopWireMock() {
        Optional.ofNullable(wireMockServer)
                .filter(WireMockServer::isRunning)
                .ifPresent(WireMockServer::stop);
        if (System.getProperty(MOCK_SERVER_BASE_URL_PROPERTY) != null)
            System.clearProperty(MOCK_SERVER_BASE_URL_PROPERTY);

        log.info("WireMock stopped");
    }
}