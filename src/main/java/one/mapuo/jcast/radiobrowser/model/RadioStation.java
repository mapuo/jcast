package one.mapuo.jcast.radiobrowser.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Created by @author mapuo on 27/12/2019.
 */
@Data
public class RadioStation {

  Long id;

  @JsonAlias("changeuuid")
  String changeUuid;

  @JsonAlias("stationuuid")
  String stationUuid;

  String name;

  String url;

  @JsonAlias("url_resolved")
  String urlResolved;

  String homepage;

  String favicon;

  String tags;

  String country;

  @JsonAlias("countrycode")
  String countryCode;

  String state;

  String language;

  Long votes;

  @JsonAlias("lastchangetime")
  String lastChangeTime;

  String ip;

  String codec;

  Long bitrate;

  Long hls;

  @JsonAlias("lastcheckok")
  Long lastCheckOk;

  @JsonAlias("lastchecktime")
  String lastCheckTime;

  @JsonAlias("lastcheckoktime")
  String lastCheckOkTime;

  @JsonAlias("clicktimestamp")
  String clickTimestamp;

  @JsonAlias("clickcount")
  Long clickCount;

  @JsonAlias("clicktrend")
  Long clickTrend;

}
