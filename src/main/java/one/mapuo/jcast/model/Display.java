package one.mapuo.jcast.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * Created by @author mapuo on 26/12/2019.
 */
@Data
public class Display implements Item {

  final String type = "Display";

  @JacksonXmlProperty(localName = "Display")
  final String text;

}
