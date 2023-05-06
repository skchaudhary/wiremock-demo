package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThan;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThan;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.StringConstants.ADD_MOVIE_V1;
import static com.learnwiremock.constants.StringConstants.GET_ALL_MOVIES_V1;
import static com.learnwiremock.constants.StringConstants.GET_MOVIE_BY_NAME;
import static com.learnwiremock.constants.StringConstants.GET_MOVIE_BY_YEAR;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WireMockExtension.class)
class MoviesRestClientTest {
    @InjectServer
    private WireMockServer wireMockServer;

    @ConfigureWireMock
    private Options options = wireMockConfig().port(8088)
        .notifier(new ConsoleNotifier(true)) // to enable notifier to provide more info on console which helps debugging
        .extensions(new ResponseTemplateTransformer(true));//for enabling feature to transform the response dynamically


    private MoviesRestClient moviesRestClient;
    private WebClient webClient;
    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl: " + baseUrl);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void getAllMovies() {
        //given
        stubFor(get(anyUrl())
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.getAllMovies();
        System.out.println("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
    }

    @Test
    void getAllMoviesMatchingCase() {
        //given
        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.getAllMovies();
        System.out.println("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
    }

    @Test
    void getMovieById() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie.json")));
        //when
        int movieId = 1;
        Movie movie = moviesRestClient.getMovieById(movieId);
        System.out.println("movieList: " + movie);

        //then
        assertNotNull(movie);
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void getMovieByIdResponseTemplating() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie-template.json")));
        //when
        int movieId = 9;
        Movie movie = moviesRestClient.getMovieById(movieId);
        System.out.println("movieList: " + movie);

        //then
        assertNotNull(movie);
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void getMovieByIdNotFound() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-movieId.json")));
        //given
        Integer movieId = 100;
        //then
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.getMovieById(movieId));
    }

    @Test
    void getMovieByName() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(GET_MOVIE_BY_NAME + "?movie_name=" + movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("avengers.json")));
        //when
        List<Movie> movieList = moviesRestClient.getMoviesByName(movieName);
        System.out.println("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
        assertEquals(4, movieList.size());
    }

    @Test
    void getMovieByNameApproach2() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(GET_MOVIE_BY_NAME))
            .withQueryParam("movie_name", equalTo(movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("avengers.json")));
        //when
        List<Movie> movieList = moviesRestClient.getMoviesByName(movieName);
        System.out.println("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
        assertEquals(4, movieList.size());
    }

    @Test
    void getMovieByNameResponseTemplating() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(GET_MOVIE_BY_NAME))
            .withQueryParam("movie_name", equalTo(movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie-by-name-template.json")));
        //when
        List<Movie> movieList = moviesRestClient.getMoviesByName(movieName);
        System.out.println("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
        assertEquals(4, movieList.size());
    }

    @Test
    void getMovieByNameNotFound() {
        //given
        String movieName = "DDLJ";
        stubFor(get(urlPathEqualTo(GET_MOVIE_BY_NAME))
            .withQueryParam("movie_name", equalTo(movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-moviename.json")));
        //then
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.getMoviesByName(movieName));
    }


    @Test
    void getMovieByYear() {
        //given
        String year = "2012";
        stubFor(get(urlPathEqualTo(GET_MOVIE_BY_YEAR))
            .withQueryParam("year", equalTo(year))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("year-template.json")));
        //when
        List<Movie> movieList = moviesRestClient.getMoviesByYear(year);
        System.out.println("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
        assertEquals(2, movieList.size());
    }

    @Test
    void getMovieByYearNotFound() {
        //given
        String year = "2030";
        stubFor(get(urlPathEqualTo(GET_MOVIE_BY_YEAR))
            .withQueryParam("year", equalTo(year))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-by-year.json")));
        //then
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.getMoviesByYear(year));
    }

    @Test
    void addMovie() {
        //given
        Movie movie = new Movie(null, "Toy Story 4", "Tom Hanks, Tim Allen",  LocalDate.of(2025, 4, 20), 2025);
        //given
        stubFor(post(urlEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie.json")));

        //when
        Movie addedMovie = moviesRestClient.addMovie(movie);
        //then
        assertNotNull(addedMovie.getMovie_id());
    }

    @Test
    void addMovieTemplating() {
        //given
        Movie movie = new Movie(null, "Toy Story 4", "Tom Hanks, Tim Allen",  LocalDate.of(2025, 4, 20), 2025);
        //given
        stubFor(post(urlEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie-template.json")));

        //when
        Movie addedMovie = moviesRestClient.addMovie(movie);
        System.out.println("added movie: " + addedMovie);
        //then
        assertNotNull(addedMovie.getMovie_id());
    }

    @Test
    void addMovieBadRequest() {
        //given
        Movie movie = new Movie(null, null, "Tom Hanks, Tim Allen",  LocalDate.of(2025, 4, 20), 2025);
        stubFor(post(urlEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-invalid-input.json")));
        //then
        String errorMessage = "Please pass all the input fields : [name]";
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.addMovie(movie), errorMessage);

    }

    @Test
    void updateMovie() {
        //given
        Integer movieId = 3;
        String cast = "ABC";
        Movie movie = new Movie(null, null, cast,  null, null);
        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("update-movie-template.json")));
        //when
        Movie addedMovie = moviesRestClient.updateMovie(movie, movieId);
        //then
        assertTrue(addedMovie.getCast().contains(cast));
    }

    @Test
    void updateMovieBadRequest() {
        //given
        Integer movieId = 100;
        String cast = "ABC";
        Movie movie = new Movie(null, null, cast,  null, null);
        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .withRequestBody(matchingJsonPath("$.cast", containing(cast)))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("update-movie-template.json")));
        //then
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.updateMovie(movie, movieId));
    }

    @Test
    void deleteMovie() {
        //given
        Movie movie = new Movie(null, "Toy Story 4", "Tom Hanks, Tim Allen",  LocalDate.of(2025, 4, 20), 2025);
        stubFor(post(urlEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie-template.json")));
        String expectedResponse = "Movie Deleted Successfully";
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(expectedResponse)));
        Movie addedMovie = moviesRestClient.addMovie(movie);

        //when
        String responseMessage = moviesRestClient.deleteMovie(addedMovie.getMovie_id());
        //then
        assertEquals(expectedResponse, responseMessage);
    }

    @Test
    void deleteMovieNotFound() {
        //given
        Long movieId = 100L;
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
        //then
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.deleteMovie(movieId));

    }

    @Test
    void deleteMovieByName() {
        //given
        Movie movie = new Movie(null, "Toy Story 4", "Tom Hanks, Tim Allen",  LocalDate.of(2025, 4, 20), 2025);
        stubFor(post(urlEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath("$.cast", containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie-template.json")));
        String expectedResponse = "Movie Deleted Successfully";
        stubFor(delete(urlEqualTo("/movieservice/v1/movieName?movie_name=Toy%20Story%204"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(expectedResponse)));
        Movie addedMovie = moviesRestClient.addMovie(movie);

        //when
        String responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());
        //then
        assertEquals(expectedResponse, responseMessage);
        verify(postRequestedFor(urlEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath("$.cast", containing("Tom"))));
        verify(exactly(1), deleteRequestedFor(urlEqualTo("/movieservice/v1/movieName?movie_name=Toy%20Story%204")));
        verify(moreThan(0), deleteRequestedFor(urlEqualTo("/movieservice/v1/movieName?movie_name=Toy%20Story%204")));
        verify(lessThan(2), deleteRequestedFor(urlEqualTo("/movieservice/v1/movieName?movie_name=Toy%20Story%204")));
    }
}