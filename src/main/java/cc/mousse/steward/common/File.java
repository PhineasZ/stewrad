package cc.mousse.steward.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PhineasZ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {

  /** 文件ID */
  private String id;
  /** 文件名 */
  private String name;
  /** 文件大小（字节数） */
  private String size;
  /** busid（目前不清楚有什么作用） */
  @JsonProperty("busid")
  private String busId;
  /** 下载链接 */
  private String url;
}
