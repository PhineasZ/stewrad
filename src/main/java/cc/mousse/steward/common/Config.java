package cc.mousse.steward.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 统一配置信息
 *
 * @author PhineasZ
 */
@Data
@Component
public class Config {

  private int serverPort;
  private int idLimit;
  private int requestLevel;
  private String reportGroupId;
  private String listenGroupId;

  @Value("${server.port}")
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  @Value("${steward.player-id-limit}")
  public void setIdLimit(int idLimit) {
    this.idLimit = idLimit;
  }

  @Value("${steward.request-level}")
  public void setRequestLevel(int requestLevel) {
    this.requestLevel = requestLevel;
  }

  @Value("${steward.report-group-id}")
  public void setReportGroupId(String reportGroupId) {
    this.reportGroupId = reportGroupId;
  }

  @Value("${steward.listen-group-id}")
  public void setListenGroupId(String listenGroupId) {
    this.listenGroupId = listenGroupId;
  }
}
