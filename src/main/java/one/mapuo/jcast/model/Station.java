package one.mapuo.jcast.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by @author mapuo on 26/12/2019.
 */
@Data
@Builder
@AllArgsConstructor
public class Station implements Item {

  @JacksonXmlProperty(localName = "ItemType")
  final String type = "Station";

  @JacksonXmlProperty(localName = "StationId")
  final String uid;

  @JacksonXmlProperty(localName = "StationName")
  final String name;

  @JacksonXmlProperty(localName = "StationDesc")
  final String description;

  @JacksonXmlProperty(localName = "StationUrl")
  final String url;

  @JacksonXmlProperty(localName = "Logo")
  final String icon;

  @JacksonXmlProperty(localName = "StationFormat")
  final String genre;

  @JacksonXmlProperty(localName = "StationLocation")
  final String location;

  @JacksonXmlProperty(localName = "StationMime")
  final String mime;

  @JacksonXmlProperty(localName = "StationBandWidth")
  final String bitrate;

  @JacksonXmlProperty(localName = "Bookmark")
  final String bookmark;

  @JacksonXmlProperty(localName = "Relia")
  final Integer relia = 3;

}
