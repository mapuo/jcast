package one.mapuo.jcast.radiobrowser;

import static org.springframework.http.HttpStatus.OK;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.mapuo.jcast.controller.model.QueryParams;
import one.mapuo.jcast.model.Directory;
import one.mapuo.jcast.model.Page;
import one.mapuo.jcast.model.Station;
import one.mapuo.jcast.radiobrowser.model.Category;
import one.mapuo.jcast.radiobrowser.model.RadioStation;
import one.mapuo.jcast.radiobrowser.model.StationUrl;
import one.mapuo.jcast.radiobrowser.model.Stats;
import one.mapuo.jcast.service.StationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by @author mapuo on 27/12/2019.
 */
@Slf4j
@Service
@Configuration
public class RadioBrowserStations implements StationProvider {

  @Getter
  final String path = "radiobrowser";
  @Getter
  final MessageSource messageSource;

  final HttpHeaders headers = new HttpHeaders();
  final RestTemplate restTemplate = new RestTemplate();
  final URI baseUri;

  public RadioBrowserStations(
      BuildProperties buildProperties,
      @Value("${radio.browser.url:.}") String url,
      @Qualifier("messageSource") MessageSource messageSource) {

    String userAgent = buildProperties.getName() + "/" + buildProperties.getVersion();
    log.debug("userAgent: {}", userAgent);

    this.messageSource = messageSource;
    headers.add(HttpHeaders.USER_AGENT, userAgent);
    baseUri = getUri(url);
  }

  private URI getUri(String url) {
    if (url != null && !url.isBlank()) {
      try {
        URI uri = new URL(url).toURI();
        if (checkApiAvailability(uri)) {
          log.info("Manually Selected RadioBrowser Endpoint: {}", uri);
          return uri;
        }
      } catch (Exception e) {
        log.warn("Invalid url '{}' for Radio Browser API!", url);
      }
    }

    try {
      // add all round robin servers one by one to select them separately
      InetAddress[] domains = InetAddress.getAllByName("all.api.radio-browser.info");
      List<String> domainList = Arrays.stream(domains)
          .map(InetAddress::getCanonicalHostName)
          .map(host -> "https://" + host)
          .collect(Collectors.toList());
      Collections.shuffle(domainList);
      URI selected = domainList.stream()
          .map(this::toUriNoThrow)
          .filter(Objects::nonNull)
          .takeWhile(this::checkApiAvailability)
          .findAny()
          .get();
      log.info("Selected RadioBrowser Endpoint: {}", selected);

      return selected;
    } catch (Exception e) {
      log.warn("No available RadioBrowser API endpoints!");
      log.error("Error getting RadioBrowser endpoint!", e);
    }

    return null;
  }

  private URI toUriNoThrow(String host) {
    try {
      return new URL(host).toURI();
    } catch (Exception ignore) {
    }
    return null;
  }

  private boolean checkApiAvailability(URI uri) {
    URI statsUri = UriComponentsBuilder.fromUri(uri)
        .pathSegment("json", "stats")
        .build()
        .encode()
        .toUri();
    RequestEntity<Void> requestEntity = RequestEntity
        .get(statsUri)
        .headers(headers)
        .build();
    ResponseEntity<Stats> responseEntity = restTemplate
        .exchange(requestEntity, Stats.class);

    HttpStatus status = responseEntity.getStatusCode();
    log.debug("responseEntity.code: {}", status);

    if (status == OK) {
      // throw new RuntimeException("API not available!");
      log.info("Server health of '{}': {}", uri, responseEntity.getBody());
      return true;
    }

    return false;
  }

  @Override
  public boolean isAvailable() {
    return baseUri != null;
  }

  @Override
  public Integer getSize() {
    return 4; // Magic number
  }

  @Override
  public Station getStation(String filter, String name, QueryParams params) {
    if ("id".equalsIgnoreCase(filter) && name != null && !name.isBlank()) {
      StationUrl stationById = getStationById(name);
      return Station.builder()
          .uid(stationById.getStationUuid())
          .name(stationById.getName())
          .url(stationById.getUrl())
          .build();
    }
    return null;
  }

  @Override
  public Page<?> getStations(String filter, String name, QueryParams params) {
    if (filter == null && name == null) {
      List<Directory> categories = Set.of("tags", "countries", "languages"/*, "topvotes"*/).stream()
          .map(type -> {
            String title = messageSource.getMessage(
                "provider.radiobrowser." + type, null, params.getLocale());
            return new Directory(title, getControllerUri(type), getCategory(type).size());
          })
          .skip(params.getPage())
          .limit(params.getSize())
          .collect(Collectors.toList());

      return new Page<>(categories, categories.size());
    } else if (filter != null && name == null) {
      // Get category
      List<Category> categoryList = getCategory(filter);
      List<Directory> directories = categoryList.stream()
          .map(category -> new Directory(category.getName(),
              getControllerUri(filter, category.getName()), category.getStations()))
          .skip(params.getPage())
          .limit(params.getSize())
          .collect(Collectors.toList());

      return new Page<>(directories, categoryList.size(), true);
    } else if (filter != null && name != null) {
      // Get stations
      List<RadioStation> stationList = getStationsByCategory(filter, name);
      List<Station> stations = stationList.stream()
          .map(station -> {
            // Create new URL for our controller - we will redirect it when hit
            String url = getControllerUri("id", String.valueOf(station.getId()));

            return new Station(
                station.getStationUuid(),
                station.getName(),
                station.getHomepage(),
                url,
                station.getFavicon(),
                station.getTags(),
                station.getCountry(),
                station.getCodec(),
                station.getBitrate().toString(),
                null
            );
          })
          .skip(params.getPage())
          .limit(params.getSize())
          .collect(Collectors.toList());

      return new Page<>(stations, stationList.size());
    }

    return null;
  }

  private List<Category> getCategory(String category) {
    if (category.equals("topvotes")) {
      return Collections.emptyList();
    }

    URI uri = UriComponentsBuilder.fromUri(baseUri)
        .pathSegment("json", category)
        .queryParam("hidebroken", Boolean.TRUE)
        .build()
        .encode()
        .toUri();
    RequestEntity<Void> requestEntity = RequestEntity
        .get(uri)
        .headers(headers)
        .build();
    ResponseEntity<List<Category>> responseEntity = restTemplate
        .exchange(requestEntity, new ParameterizedTypeReference<>() {
        });
    log.debug("responseEntity.code: {}", responseEntity.getStatusCode());

    return responseEntity.getBody();
  }

  private List<RadioStation> getStationsByCategory(String category, String name) {
    Map<String, String> categories = Map.of(
        "countries", "bycountry",
        "languages", "bylanguage",
        "tags", "bytag"
    );

    URI uri = UriComponentsBuilder.fromUri(baseUri)
        .pathSegment("json", "stations", categories.get(category), name)
        .queryParam("hidebroken", Boolean.TRUE)
        .build()
        .encode()
        .toUri();
    RequestEntity<Void> requestEntity = RequestEntity
        .get(uri)
        .headers(headers)
        .build();
    ResponseEntity<List<RadioStation>> responseEntity = restTemplate
        .exchange(requestEntity, new ParameterizedTypeReference<>() {
        });
    log.debug("responseEntity.code: {}", responseEntity.getStatusCode());

    return responseEntity.getBody();
  }

  private StationUrl getStationById(String id) {
    URI uri = UriComponentsBuilder.fromUri(baseUri)
        .pathSegment("json", "url", id)
        .queryParam("hidebroken", Boolean.TRUE)
        .build()
        .encode()
        .toUri();
    RequestEntity<Void> requestEntity = RequestEntity
        .get(uri)
        .headers(headers)
        .build();
    ResponseEntity<StationUrl> responseEntity = restTemplate
        .exchange(requestEntity, StationUrl.class);
    log.debug("responseEntity.code: {}", responseEntity.getStatusCode());

    return responseEntity.getBody();
  }

}
