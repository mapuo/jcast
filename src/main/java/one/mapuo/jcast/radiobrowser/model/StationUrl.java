package one.mapuo.jcast.radiobrowser.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Created by @author mapuo on 29/12/2019.
 */
@Data
public class StationUrl {

  Boolean ok;

  String message;

  Integer id;

  @JsonAlias("stationuuid")
  String stationUuid;

  String name;

  String url;

}
