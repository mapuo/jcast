package one.mapuo.jcast.radiobrowser.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Created by @author mapuo on 27/12/2019.
 */
@Data
public class Category {

  String name;

  @JsonAlias("stationcount")
  Integer stations;

}
