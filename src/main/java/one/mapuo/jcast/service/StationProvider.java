package one.mapuo.jcast.service;

import java.util.Locale;
import one.mapuo.jcast.controller.HomeController;
import one.mapuo.jcast.controller.model.QueryParams;
import one.mapuo.jcast.model.Directory;
import one.mapuo.jcast.model.Page;
import one.mapuo.jcast.model.Station;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by @author mapuo on 27/12/2019.
 */
public interface StationProvider {

  String getPath();

  boolean isAvailable();

  Integer getSize();

  Page<?> getStations(String filter, String name, QueryParams params);

  MessageSource getMessageSource();

  default Station getStation(String filter, String name, QueryParams params) {
    return null;
  }

  default String getDisplayName() {
    return getDisplayName(Locale.getDefault());
  }

  default String getDisplayName(Locale locale) {
    String code = "provider." + getPath() + ".title";
    String defaultMessage = StringUtils.capitalize(getPath());
    return getMessageSource().getMessage(code, null, defaultMessage, locale);
  }

  default Directory getDirectory() {
    return getDirectory(Locale.getDefault());
  }

  default Directory getDirectory(Locale locale) {
    String destination = getControllerUri().build().encode().toString();
    return Directory.builder()
        .title(getDisplayName(locale))
        .destination(destination)
        .destinationBackup(destination)
        .itemCount(getSize())
        .build();
  }

  default UriComponentsBuilder getControllerUri() {
    return MvcUriComponentsBuilder
        .fromMethodName(HomeController.class, "provider",
            null, null, null, null)
        .path(getPath())
        .queryParam("empty", "");
  }

  default String getControllerUri(String... pathSegment) {
    return getControllerUri()
        .pathSegment(pathSegment)
        .build()
        .encode()
        .toString();
  }

}
