package one.mapuo.jcast.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by @author mapuo on 26/12/2019.
 */
@Data
@AllArgsConstructor
@Builder
public class Directory implements Item {

  final String type = "Dir";

  @JacksonXmlProperty(localName = "Title")
  final String title;

  @JacksonXmlProperty(localName = "UrlDir")
  final String destination;

  @JacksonXmlProperty(localName = "UrlDirBackUp")
  final String destinationBackup;

  @JacksonXmlProperty(localName = "DirCount")
  final Integer itemCount;

  public Directory(String title, String destination, Integer itemCount) {
    this.title = title;
    this.destination = destination;
    this.destinationBackup = destination;
    this.itemCount = itemCount;
  }

  public Directory(String title, String destination) {
    this.title = title;
    this.destination = destination;
    this.destinationBackup = destination;
    this.itemCount = -1;
  }

}
