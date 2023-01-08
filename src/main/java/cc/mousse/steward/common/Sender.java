package cc.mousse.steward.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author PhineasZ
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sender {
  private String age;
  private Object area;
  private String card;
  private Integer level;
  private String nickname;
  private String role;
  private String sex;
  private String title;

  @JsonProperty("group_id")
  private String groupId;

  @JsonProperty("user_id")
  private String userId;
}
