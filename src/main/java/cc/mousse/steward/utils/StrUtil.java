package cc.mousse.steward.utils;

import cc.mousse.steward.common.Config;
import lombok.val;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author PhineasZ
 */
@Component
public class StrUtil {

  private static final char QUOTE = '\"';

  @Resource private Config config;

  private StrUtil() {}

  /**
   * 获取合规角色名称
   *
   * @param originalName 群名片
   * @return idx0：角色名数量范围内；idx1：角色名数量范围外
   */
  public List<List<String>> getLegal(String originalName) {
    return getLegal(originalName, true);
  }

  /**
   * @param originalName 群名片
   * @param limit 是否启用名称数量限制
   * @return idx0：角色名数量范围内；idx1：角色名数量范围外
   */
  public List<List<String>> getLegal(String originalName, boolean limit) {
    val names = new ArrayList<List<String>>(2);
    names.add(new ArrayList<>());
    names.add(new ArrayList<>());
    val c = originalName.toCharArray();
    var builder = new StringBuilder();
    int count = 0;
    for (int i = 0; i <= c.length; i++) {
      if (i < c.length && wordLegalCheck(c[i])) {
        builder.append(c[i]);
      } else if (builder.length() != 0) {
        if (count < config.getIdLimit() || !limit) {
          // 角色名数量范围内
          names.get(0).add(builder.toString());
        } else {
          names.get(1).add(builder.toString());
        }
        builder = new StringBuilder();
        count++;
      }
    }
    return names;
  }

  private boolean wordLegalCheck(char c) {
    return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_';
  }

  public String removeQuotes(String s) {
    if (s != null) {
      if (s.charAt(0) == QUOTE) {
        s = s.substring(1);
      }
      if (s.charAt(s.length() - 1) == QUOTE) {
        s = s.substring(0, s.length() - 1);
      }
    }
    return s;
  }

  public <T> String splice(String s, List<T> data) {
    StringBuilder builder = new StringBuilder();
    val strs = s.split("\\$");
    val iter = data.iterator();
    for (String str : strs) {
      builder.append(str);
      if (iter.hasNext()) {
        builder.append(iter.next());
      }
    }
    return builder.toString();
  }

  public <T> String splice(String s, T data) {
    StringBuilder builder = new StringBuilder();
    val strs = s.split("\\$");
    boolean usedFlag = false;
    for (String str : strs) {
      builder.append(str);
      if (!usedFlag) {
        builder.append(data);
        usedFlag = true;
      }
    }
    return builder.toString();
  }
}
