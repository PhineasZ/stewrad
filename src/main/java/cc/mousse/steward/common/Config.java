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

  private String success;
  private String playerNotFound;

  private String whitelistAlreadyExists;
  private String noLegalName;
  private String outOfLimit;

  private String noWhitelist;

  private String fakeBlacklist;

  @Value("${steward.server.port}")
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  @Value("${steward.server.player-id-limit}")
  public void setIdLimit(int idLimit) {
    this.idLimit = idLimit;
  }

  @Value("${steward.server.request-level}")
  public void setRequestLevel(int requestLevel) {
    this.requestLevel = requestLevel;
  }

  @Value("${steward.server.report-group-id}")
  public void setReportGroupId(String reportGroupId) {
    this.reportGroupId = reportGroupId;
  }

  @Value("${steward.server.listen-group-id}")
  public void setListenGroupId(String listenGroupId) {
    this.listenGroupId = listenGroupId;
  }

  @Value("${steward.message.success}")
  public void setSuccess(String success) {
    this.success = success;
  }

  @Value("${steward.message.player-not-found}")
  public void setPlayerNotFound(String playerNotFound) {
    this.playerNotFound = playerNotFound;
  }

  @Value("${steward.message.no-legal-name}")
  public void setNoLegalName(String noLegalName) {
    this.noLegalName = noLegalName;
  }

  @Value("${steward.message.out-of-limit}")
  public void setOutOfLimit(String outOfLimit) {
    this.outOfLimit = outOfLimit;
  }

  @Value("${steward.message.whitelist-already-exists}")
  public void setWhitelistAlreadyExists(String whitelistAlreadyExists) {
    this.whitelistAlreadyExists = whitelistAlreadyExists;
  }

  @Value("${steward.message.no-whitelist}")
  public void setNoWhitelist(String noWhitelist) {
    this.noWhitelist = noWhitelist;
  }

  @Value("${steward.message.fake-blacklist}")
  public void setFakeBlacklist(String fakeBlacklist) {
    this.fakeBlacklist = fakeBlacklist;
  }
}
