package cc.mousse.steward.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author PhineasZ
 */
@Data
public class Cache {

  private Map<String, String> idCardMap;

  public Cache() {
    idCardMap = new HashMap<>();
  }
}
