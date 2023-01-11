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

  @Value("${steward.server.port}")
  private int serverPort;

  @Value("${steward.server.listen-group.id}")
  private String listenGroupId;

  @Value("${steward.server.listen-group.player-id-limit}")
  private int idLimit;

  @Value("${steward.server.listen-group.request.level}")
  private int requestLevel;

  @Value("${steward.server.listen-group.request.enable-verify}")
  private boolean enableVerify;

  @Value("${steward.server.listen-group.request.question}")
  private String question;

  @Value("${steward.server.listen-group.request.answer}")
  private String answer;

  @Value("${steward.server.report-group.id}")
  private String reportGroupId;

  @Value("${steward.server.cache-path}")
  private String cachePath;

  @Value("${steward.message.success}")
  private String success;

  @Value("${steward.message.player-not-found}")
  private String playerNotFound;

  @Value("${steward.message.whitelist-already-exists}")
  private String whitelistAlreadyExists;

  @Value("${steward.message.no-legal-name}")
  private String noLegalName;

  @Value("${steward.message.out-of-limit}")
  private String outOfLimit;

  @Value("${steward.message.no-whitelist}")
  private String noWhitelist;

  @Value("${steward.message.fake-blacklist}")
  private String fakeBlacklist;
}
