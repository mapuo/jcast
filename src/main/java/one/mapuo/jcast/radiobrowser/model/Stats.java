package one.mapuo.jcast.radiobrowser.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Created by @author mapuo on 29/12/2019.
 */
@Data
public class Stats {

  @JsonAlias("supported_version")
  Integer supportedVersion;

  @JsonAlias("software_version")
  String softwareVersion;

  String status;

  Integer stations;

  @JsonAlias("stations_broken")
  Integer brokenStations;

  Integer tags;

  @JsonAlias("clicks_last_hour")
  Integer clicksLastHour;

  @JsonAlias("clicks_last_day")
  Integer clicksLastDay;

  Integer languages;

  Integer countries;

}
