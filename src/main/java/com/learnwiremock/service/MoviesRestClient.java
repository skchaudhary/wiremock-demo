package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.learnwiremock.constants.StringConstants.ADD_MOVIE_V1;
import static com.learnwiremock.constants.StringConstants.GET_ALL_MOVIES_V1;
import static com.learnwiremock.constants.StringConstants.GET_MOVIE_BY_ID;
import static com.learnwiremock.constants.StringConstants.GET_MOVIE_BY_NAME;
import static com.learnwiremock.constants.StringConstants.GET_MOVIE_BY_YEAR;

@Slf4j
public class MoviesRestClient {

    private WebClient webClient;

    public MoviesRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Movie> getAllMovies() {
        try {
            return webClient.get().uri(GET_ALL_MOVIES_V1)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in getAllMovies, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in getAllMovies and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie getMovieById(Integer movieId) {
        try {
            return webClient.get().uri(GET_MOVIE_BY_ID, movieId)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in getAllMovieById, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in getAllMovieById and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> getMoviesByName(String name) {
        // http://localhost:8081/movieservice/v1/movieName?movie_name=Avengers
        String urlToGetMovieByName = UriComponentsBuilder.fromUriString(GET_MOVIE_BY_NAME)
            .queryParam("movie_name", name)
            .buildAndExpand()
            .toUriString();
        try {
            return webClient.get().uri(urlToGetMovieByName)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in getMoviesByName, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in getMoviesByName and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> getMoviesByYear(String name) {
//        http://localhost:8081/movieservice/v1/movieYear?year=2012
        String urlToGetMovieByYear = UriComponentsBuilder.fromUriString(GET_MOVIE_BY_YEAR)
            .queryParam("year", name)
            .buildAndExpand()
            .toUriString();
        try {
            return webClient.get().uri(urlToGetMovieByYear)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in getMoviesByYear, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in getMoviesByYear and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }


    public Movie addMovie(Movie movie) {
//       "http://localhost:8081/movieservice/v1/movie"
        try {
            return webClient.post().uri(ADD_MOVIE_V1)
                .bodyValue(movie)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in addMovie, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in addMovie and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie updateMovie(Movie movie, Integer id) {
//       "http://localhost:8081/movieservice/v1/movie/1"
        try {
            return webClient.put().uri(GET_MOVIE_BY_ID, id)
                .bodyValue(movie)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in updateMovie, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in updateMovie and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public String deleteMovie(Long id) {
//       "http://localhost:8081/movieservice/v1/movie/1"
        try {
            return webClient.delete().uri(GET_MOVIE_BY_ID, id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in deleteMovie, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in deleteMovie and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public String deleteMovieByName(String movieName) {
//       "http://localhost:8081/movieservice/v1/movie?movie_name=?"
        try {
            String urlToGetMovieByName = UriComponentsBuilder.fromUriString(GET_MOVIE_BY_NAME)
                .queryParam("movie_name", movieName)
                .buildAndExpand()
                .toUriString();
            webClient.delete().uri(urlToGetMovieByName)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in deleteMovie, status code: {} and response body is {}.", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in deleteMovie and the message is {0}", ex);
            throw new MovieErrorResponse(ex);
        }
        return "Movie Deleted Successfully";
    }
}
