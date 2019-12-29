package one.mapuo.jcast.favorites;

import static com.google.common.hash.Hashing.goodFastHash;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.io.File;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.mapuo.jcast.controller.model.QueryParams;
import one.mapuo.jcast.favorites.model.Category;
import one.mapuo.jcast.model.Directory;
import one.mapuo.jcast.model.Page;
import one.mapuo.jcast.model.Station;
import one.mapuo.jcast.service.StationProvider;
import one.mapuo.jcast.service.WatcherService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * Created by @author mapuo on 27/12/2019.
 */
@Slf4j
@Service
@Configuration
public class FavoriteStations implements StationProvider {

  @Getter
  final String path = "favorites";
  @Getter
  final MessageSource messageSource;

  final WatcherService watcherService;
  final File stationDefinitions;
  final Supplier<List<Category>> readStations = this::readStations;
  volatile Supplier<List<Category>> memoized = Suppliers.memoize(readStations);
  List<Category> favoritesList;

  public FavoriteStations(
      @Value("${favorites.file:.}") String stationDefinitions,
      @Qualifier("messageSource") MessageSource messageSource,
      WatcherService watcherService) {

    log.debug("stationDefinitions: {}", stationDefinitions);

    this.messageSource = messageSource;
    this.watcherService = watcherService;
    this.stationDefinitions = new File(stationDefinitions);
    this.favoritesList = memoized.get();
  }

  @PostConstruct
  public void postConstruct() {
    watcherService.add(stationDefinitions, this::reloadStations);
    log.info("Added '{}' to watch!", stationDefinitions);
  }

  @PreDestroy
  public void preDestroy()
      throws Exception {

    watcherService.destroy();
  }

  private void reloadStations(WatchEvent<?> watchEvent) {
    memoized = Suppliers.memoize(readStations);
    this.favoritesList = memoized.get();
    log.info("Stations reloaded!");
  }

  private List<Category> readStations() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.findAndRegisterModules();
      List<Category> stations =
          mapper.readValue(stationDefinitions, new TypeReference<>() {
          });
      log.info("stations: {}", stations);
      return stations;
    } catch (Exception e) {
      log.warn("Error reading favorites file: {}!", stationDefinitions);
      // throw new RuntimeException("Error reading file!", e);
    }
    return null;
  }

  @Override
  public boolean isAvailable() {
    return favoritesList != null && favoritesList.size() > 0;
  }

  @Override
  public Integer getSize() {
    return favoritesList != null ? favoritesList.size() : null;
  }

  @Override
  public Page<?> getStations(String filter, String name, QueryParams params) {
    log.debug("filter: '{}', name: '{}'", filter, name);
    if (filter == null) {
      return getCategories(params);
    } else {
      return getStations(filter, params);
    }
  }

  public Page<Directory> getCategories(QueryParams params) {
    List<Directory> categories = favoritesList.stream()
        .map(category -> {
          String uri = getControllerUri(category.getName());
          return new Directory(category.getName(), uri, category.getStations().size());
        })
        .skip(params.getPage())
        .limit(params.getSize())
        .collect(Collectors.toList());

    return new Page<>(categories, favoritesList.size());
  }

  public Page<Station> getStations(String category, QueryParams params) {
    Category definedCategory = favoritesList.stream()
        .filter(cat -> cat.getName().equalsIgnoreCase(category))
        .findFirst()
        .orElseThrow();
    List<Station> stations = definedCategory.getStations().stream()
        .map(definedStation -> {
          String uid = goodFastHash(12)
              .hashString(definedStation.getName() + definedStation.getUrl(), UTF_8)
              .toString()
              .toUpperCase();
          return new Station(
              uid,
              definedStation.getName(),
              null,
              definedStation.getUrl(),
              definedStation.getIcon(),
              category,
              null,
              null,
              null,
              null);
        })
        .skip(params.getPage())
        .limit(params.getSize())
        .collect(Collectors.toList());

    return new Page<>(stations, definedCategory.getStations().size());
  }

}
