package one.mapuo.jcast.controller.model;

import java.util.Locale;
import lombok.Data;

/**
 * Created by @author mapuo on 27/12/2019.
 */
@Data
public class QueryParams {

  Integer token;

  String mac;

  String version;

  Locale locale = new Locale("eng");

  Integer start = 1;

  Integer end = 10;

  public Integer getPage() {
    return start == 1 ? 0 : start;
  }

  public Integer getSize() {
    return end;
  }

  public boolean isTokenRequest() {
    return token != null/* && token == 0*/;
  }

  // Hacky way to make aliases

  public void setFver(String version) {
    this.version = version;
  }

  public void setDlang(Locale locale) {
    this.locale = locale;
  }

  public void setStartitems(Integer start) {
    this.start = start;
  }

  public void setEnditems(Integer end) {
    this.end = end;
  }

  public void setHowmany(Integer end) {
    this.end = getPage() + end;
  }

}
