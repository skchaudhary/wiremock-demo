package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.learnwiremock.exception.MovieErrorResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientServerFaultTest {
    private MoviesRestClient moviesRestClient;
    private WebClient webClient;
    @InjectServer
    private WireMockServer wireMockServer;

    @ConfigureWireMock
    private Options options = wireMockConfig().port(8088)
        .notifier(new ConsoleNotifier(true)) // to enable notifier to provide more info on console which helps debugging
        .extensions(new ResponseTemplateTransformer(true));//for enabling feature to transform the response dynamically

    private HttpClient tcpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .doOnConnected(connection -> {
            connection.addHandlerFirst(new ReadTimeoutHandler(5))
                .addHandlerFirst(new WriteTimeoutHandler(5));
        });

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl: " + baseUrl);
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(tcpClient))
            .baseUrl(baseUrl).build();
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void getAllMoviesWithServerError() {
        //given
        stubFor(get(anyUrl()).willReturn(serverError()));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
    }

    @Test
    void getAllMoviesWithServerError503() {
        //given
        String responseMessage = "Service Unavailable";
        stubFor(get(anyUrl())
            .willReturn(serverError()
                .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .withBody(responseMessage)));

        //when
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
        assertEquals(responseMessage, movieErrorResponse.getMessage());
    }

    @Test
    void getAllMoviesWithFaultResponse() {
        //given
        stubFor(get(anyUrl())
            .willReturn(aResponse()
                .withFault(Fault.EMPTY_RESPONSE)));

        //when
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
        String errorMessage = "org.springframework.web.reactive.function.client.WebClientRequestException: Connection prematurely closed BEFORE response; nested exception is reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(errorMessage, movieErrorResponse.getMessage());
    }


    @Test
    void getAllMoviesWithRandomDataThenCLosed() {
        //given
        stubFor(get(anyUrl())
            .willReturn(aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
    }

    @Test
    void getAllMoviesWithFixedDelay() {
        //given
        stubFor(get(anyUrl())
            .willReturn(ok().withFixedDelay(10000)));

        //when
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.getAllMovies());
    }
}
