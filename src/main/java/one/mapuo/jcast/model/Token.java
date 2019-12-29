package one.mapuo.jcast.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

/**
 * Created by @author mapuo on 27/12/2019.
 */
@Data
@JacksonXmlRootElement(localName = "EncryptedToken")
public class Token {

  @JacksonXmlText
  final String token = "0000000000000000";

}
