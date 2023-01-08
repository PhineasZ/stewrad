package cc.mousse.steward.common;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回执消息
 *
 * @author PhineasZ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Receipt {

  /**
   * 状态，表示API是否调用成功<br>
   * 如果成功，则是OK<br>
   * 其他的在下面会说明
   */
  private Status status;
  /**
   * 0：调用成功<br>
   * 1：已提交async处理<br>
   * 其他：操作失败，具体原因可以看响应的msg和wording字段
   */
  @JsonProperty("retcode")
  private Integer retCode;
  /** 错误消息，仅在API调用失败式有该字段 */
  private String msg;
  /** 对错误的详细解释（中文），仅在API调用失败时有该字段 */
  private String wording;
  /** 返回的具体数据 */
  private JsonNode data;

  /** '回声'，如果请求时指定了echo，那么响应也会包含echo */
  private String echo;

  enum Status {
    /**
     * OK：调用成功<br>
     * ASYNC：已提交async处理<br>
     * FAILED：操作失败
     */
    @JsonProperty("ok")
    OK,
    @JsonProperty("async")
    ASYNC,
    @JsonProperty("failed")
    FAILED
  }
}
