package cc.mousse.steward.service.impl;

import cc.mousse.steward.common.Config;
import cc.mousse.steward.domain.Cache;
import cc.mousse.steward.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author PhineasZ
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Resource private Config config;

  @Override
  public void addOne(String userId, String card) {
    var cache = read();
    cache.getIdCardMap().put(userId, card);
    write(cache);
  }

  @Override
  public String deleteOne(String userId) {
    var card = "";
    var cache = read();
    var map = cache.getIdCardMap();
    if (map.containsKey(userId)) {
      card = map.get(userId);
      map.remove(userId);
      write(cache);
    }
    return card;
  }

  private Cache read() {
    var file = new File(config.getCachePath());
    try {
      return MAPPER.readValue(file, Cache.class);
    } catch (IOException e) {
      return new Cache();
    }
  }

  public void write(Cache cache) {
    try (var writer = new FileWriter(config.getCachePath())) {
      writer.write(MAPPER.writeValueAsString(cache));
      writer.flush();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}
