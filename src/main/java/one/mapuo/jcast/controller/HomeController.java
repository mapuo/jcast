package one.mapuo.jcast.controller;

import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import one.mapuo.jcast.controller.model.QueryParams;
import one.mapuo.jcast.model.Directory;
import one.mapuo.jcast.model.Page;
import one.mapuo.jcast.model.Station;
import one.mapuo.jcast.model.Token;
import one.mapuo.jcast.service.StationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by @author mapuo on 26/12/2019.
 */
@Slf4j
@Controller
@RequestMapping(value = "/", produces = APPLICATION_XML_VALUE)
public class HomeController {

  final MessageSource messages;
  final Set<StationProvider> providers;

  public HomeController(
      @Qualifier("messageSource") MessageSource messages,
      Set<StationProvider> providers) {

    this.messages = messages;
    this.providers = providers;
  }

  @PostConstruct
  public void logProviders() {
    Set<String> providersSet = providers.stream()
        .filter(StationProvider::isAvailable)
        .map(StationProvider::getDisplayName)
        .collect(Collectors.toSet());
    log.info("Available providers: {}", providersSet);
  }

  @GetMapping
  public ResponseEntity<Page<?>> index(final QueryParams params) {

    log.debug("params: {}", params);

    List<Directory> directories = providers.stream()
        .filter(StationProvider::isAvailable)
        .map(service -> service.getDirectory(params.getLocale()))
        .skip(params.getPage())
        .limit(params.getSize())
        .collect(Collectors.toList());

    Page<Directory> page = new Page<>(directories);

    return ResponseEntity
        .ok(page);
  }

  // /setupapp/Yamaha/asp/BrowseXML/loginXML.asp
  @GetMapping("/setupapp/{manufacturer}/**/loginXML.asp")
  public ResponseEntity<?> setup(
      @PathVariable final String manufacturer,
      final QueryParams params) {

    log.debug("manufacturer: {}", manufacturer);
    log.debug("params: {}", params);

    if (params.isTokenRequest()) {
      return encryptedToken();
    }

    return index(params);
  }

  @GetMapping({
      "/{provider}",
      "/{provider}/{filter}",
      "/{provider}/{filter}/{name}"
  })
  public ResponseEntity<?> provider(
      @PathVariable final String provider,
      @PathVariable(required = false) final String filter,
      @PathVariable(required = false) final String name,
      final QueryParams params)
      throws Exception {

    log.debug("provider: '{}', filter: '{}', name: '{}'",
        provider, filter, name);

    log.debug("locale: {}, start: {}, end: {}",
        params.getLocale(), params.getStart(), params.getEnd());

    if (params.isTokenRequest()) {
      return encryptedToken();
    }

    Optional<StationProvider> stationProvider = providers.stream()
        .filter(service -> service.getPath().equalsIgnoreCase(provider))
        .peek(service -> log.debug("service: {}", service))
        .findFirst();

    if (stationProvider.isPresent()) {
      StationProvider sp = stationProvider.get();
      Station station = sp.getStation(filter, name, params);
      if (station != null) {
        return ResponseEntity
            .status(TEMPORARY_REDIRECT)
            .location(new URL(station.getUrl()).toURI())
            .build();
      }
    }

    Page<?> page = stationProvider
        .map(service -> service.getStations(filter, name, params))
        .orElseThrow();

    return ResponseEntity
        .ok(page);
  }

  private ResponseEntity<?> encryptedToken() {
    return ResponseEntity
        .ok(new Token());
  }


}
