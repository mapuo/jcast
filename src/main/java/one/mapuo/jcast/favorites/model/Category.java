package one.mapuo.jcast.favorites.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.mapuo.jcast.model.Station;

/**
 * Created by @author mapuo on 28/12/2019.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  String name;
  List<Station> stations;

}
