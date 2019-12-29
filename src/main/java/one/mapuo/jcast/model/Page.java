package one.mapuo.jcast.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by @author mapuo on 26/12/2019.
 */
@Data
@AllArgsConstructor
@JacksonXmlRootElement(localName = "ListOfItems")
public class Page<T extends Item> {

  @JacksonXmlProperty(localName = "Item")
  @JacksonXmlElementWrapper(localName = "Item", useWrapping = false)
  final List<T> items;

  @JacksonXmlProperty(localName = "ItemCount")
  final Integer count;

  @JacksonXmlProperty(localName = "NoDataCache")
  Boolean noCache = false;

  public Page(List<T> items, Integer count) {
    this.items = items;
    this.count = count;
  }

  public Page(List<T> items) {
    this.items = items;
    if (items != null) {
      this.count = items.size();
    } else {
      this.count = -1;
    }
  }

  public Page(List<T> items, Boolean noCache) {
    this.items = items;
    if (items != null) {
      this.count = items.size();
    } else {
      this.count = -1;
    }
    this.noCache = noCache;
  }

  public Page(T... items) {
    this.items = Arrays.asList(items);
    this.count = items.length;
  }

}
