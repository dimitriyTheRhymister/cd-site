package ru.job4j.site.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.job4j.site.dto.ProfileDTO;
import ru.job4j.site.exception.AppException;
import ru.job4j.site.exception.IdNotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class WebClientAuthCall {
    private final WebClient webClient;

    /**
     * Метод doGet отправляет get запрос в сервис AUTH.
     * При получении ошибки 404 выбрасываем IdNotFoundException,
     * при других ошибках - AppException.
     *
     * @param url   составной адрес сервиса
     * @param token "Authorization", "Bearer + token" доступ
     * @return Mono<String>
     */
    public Mono<String> doGet(String url, String token) {
        return webClient
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        return new IdNotFoundException("Ресурс не найден: " + url);
                    }
                    return new AppException("Ошибка при обращении к сервису: " + ex.getStatusCode());
                })
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    /**
     * Метод doPostMultipart отправляет post запрос в сервис AUTH.
     * Тело запроса содержит MULTIPART_FROM_DATA.
     *
     * @param url     составной адрес сервиса
     * @param token   "Authorization", "Bearer + token" доступ
     * @param builder MultipartBodyBuilder
     * @return Mono<ResponseEntity < String>>
     */
    public Mono<ResponseEntity<String>> doPostMultipart(String url, String token, MultipartBodyBuilder builder) {
        return webClient
                .post()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .toEntity(String.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        return new IdNotFoundException("Ресурс не найден: " + url);
                    }
                    return new AppException("Ошибка при обращении к сервису: " + ex.getStatusCode());
                })
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    /**
     * Метод обрабатывает запросы Get.
     *
     * @param url String URL API
     * @return Mono<ResponseEntity>
     */
    public Mono<ResponseEntity<ProfileDTO>> doGetReqParam(String url) {
        return webClient
                .get()
                .uri(urlBuilder -> urlBuilder
                        .path(url)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(ProfileDTO.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        return new IdNotFoundException("Профиль не найден: " + url);
                    }
                    return new AppException("Ошибка при обращении к сервису: " + ex.getStatusCode());
                })
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    /**
     * Метод обрабатывает запросы Get.
     *
     * @param url String URL API
     * @return Mono<ResponseEntity < List < ProfileDTO>>>
     */
    public Mono<ResponseEntity<List<ProfileDTO>>> doGetReqParamAll(String url) {
        return webClient
                .get()
                .uri(urlBuilder -> urlBuilder
                        .path(url)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(ProfileDTO.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        return new IdNotFoundException("Список профилей не найден: " + url);
                    }
                    return new AppException("Ошибка при обращении к сервису: " + ex.getStatusCode());
                })
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    /**
     * Метод обрабатывает запрос get получения изображения из сервиса Auth
     *
     * @param url img URL
     * @param id  img ID
     * @return Mono<ResponseEntity < ByteArrayResource>>
     */
    public Mono<ResponseEntity<ByteArrayResource>> doGetPhoto(String url, int id) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("id", id)
                        .build())
                .retrieve()
                .toEntity(ByteArrayResource.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        return new IdNotFoundException("Фото не найдено: " + id);
                    }
                    return new AppException("Ошибка при обращении к сервису: " + ex.getStatusCode());
                })
                .doOnError(err -> log.error("API {} not found: {}", url, id));
    }
}