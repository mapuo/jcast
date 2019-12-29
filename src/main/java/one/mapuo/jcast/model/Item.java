package one.mapuo.jcast.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Created by @author mapuo on 26/12/2019.
 */
@JacksonXmlRootElement(localName = "Item")
public interface Item {

  @JacksonXmlProperty(localName = "ItemType")
  String getType();

}
