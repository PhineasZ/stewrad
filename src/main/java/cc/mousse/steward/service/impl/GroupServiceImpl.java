package cc.mousse.steward.service.impl;

import cc.mousse.steward.common.Config;
import cc.mousse.steward.common.Event;
import cc.mousse.steward.common.Result;
import cc.mousse.steward.domain.CacheWhitelist;
import cc.mousse.steward.service.*;
import cc.mousse.steward.utils.ApiUtil;
import cc.mousse.steward.utils.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static cc.mousse.steward.common.Constant.*;

/**
 * @author PhineasZ
 */
@Slf4j
@Service
@Scope(value = "prototype")
public class GroupServiceImpl implements GroupService {

  @Resource private Config config;

  @Resource private ApiUtil apiUtil;

  @Resource private StrUtil strUtil;

  @Resource private CacheService cacheService;

  @Resource private PlayerService playerService;

  @Resource private UserDataService userDataService;

  @Resource private CacheWhitelistService cacheWhitelistService;

  /**
   * 清理白名单
   *
   * @param ctx 上下文环境
   */
  private void flushWhitelist(ChannelHandlerContext ctx) {
    log.debug("开始清理白名单");
    try {
      // 垃圾桶
      var bin = new HashSet<String>();
      // 获取所有群成员信息
      var groupMembers = apiUtil.getGroupMemberList(ctx, config.getListenGroupId());
      // 获取所有有效群名片
      var legalCards = new HashSet<String>();
      groupMembers.forEach(
          groupMember -> legalCards.addAll(strUtil.getLegal(groupMember.getCard()).get(0)));
      // blessingsink.player角色集合
      var players = new HashSet<String>();
      playerService.getAll().forEach(player -> players.add(player.getName()));
      // 遍历所有有效群名片
      // 若blessingsink.player中不存在则过滤
      legalCards.removeIf(name -> !players.contains(name));
      // 清理multilogin.multilogin_cache_whitelist表
      for (var cacheWhitelist : cacheWhitelistService.getAll()) {
        var name = cacheWhitelist.getSign();
        // 若有效群名片不存在该缓存则清除
        if (!legalCards.contains(name)) {
          cacheWhitelistService.deleteOne(cacheWhitelist);
          bin.add(name);
        }
      }
      // 清理multilogin.multilogin_user_data表
      for (var userData : userDataService.getAll()) {
        var name = userData.getCurrentName();
        // 若有效群名片不存在该缓存则更新
        if (!legalCards.contains(name) && userData.getWhitelist() == 1) {
          userData.setWhitelist(0);
          userDataService.updateOne(userData);
          bin.add(name);
        }
      }
      if (!bin.isEmpty()) {
        var message = strUtil.getString("清理 [{}] 条白名单 → {}", bin.size(), bin);
        log.info(message);
        apiUtil.sendLog(ctx, message);
      }
      log.debug("完成清理白名单");
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.warn(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 添加有限制的白名单
   *
   * @param ctx 上下文环境
   * @param event 事件
   */
  private void addWhitelist(ChannelHandlerContext ctx, Event event) {
    try {
      var userId = event.getUserId();
      // 结果集
      var results = new EnumMap<Result.Cause, Result>(Result.Cause.class);
      // 获取玩家群名片
      var groupMember = apiUtil.getGroupMemberInfo(ctx, userId);
      if (groupMember == null) {
        log.error("群成员信息获取失败");
        return;
      }
      // 获取所有有效角色名
      var names = strUtil.getLegal(groupMember.getCard());
      // 超出数量限制的角色名
      for (var illegalName : names.get(1)) {
        putResults(results, Result.outOfLimit(illegalName));
      }
      var legalNames = names.get(0);
      if (legalNames != null && !legalNames.isEmpty()) {
        // 存在有效角色名
        // 处理每个角色名
        for (var legalName : legalNames) {
          // 查看blessingsink.player是否存在该角色
          if (playerService.getOneByName(legalName) == null) {
            // blessingsink.player不存在角色
            putResults(results, Result.playerNotFound(legalName));
            continue;
          }
          addWhitelist(results, legalName);
        }
      } else {
        // 不存在有效角色名
        results.put(Result.Cause.NO_LEGAL_NAME, Result.noLegalName());
      }
      log.debug("返回结果");
      results.forEach(
          (cause, result) -> {
            String message;
            var data = result.getData();
            switch (cause) {
              case SUCCESS -> {
                try {
                  apiUtil.sendLog(ctx, strUtil.getString("添加 [{}] 条白名单 → {}", data.size(), data));
                } catch (JsonProcessingException e) {
                  log.error(e.getMessage());
                }
                message = strUtil.splice(config.getSuccess(), data);
              }
              case PLAYER_NOT_FOUND -> message = strUtil.splice(config.getPlayerNotFound(), data);
              case WHITELIST_ALREADY_EXISTS -> message =
                  strUtil.splice(config.getWhitelistAlreadyExists(), data);
              case NO_LEGAL_NAME -> message = config.getNoLegalName();
              case OUT_OF_LIMIT -> {
                var list = new ArrayList<>();
                list.add(data);
                list.add(config.getIdLimit());
                message = strUtil.splice(config.getOutOfLimit(), list);
              }
              default -> message = "未知分支";
            }
            try {
              apiUtil.sendGroupReply(
                  ctx, String.valueOf(event.getMessageId()), message, event.getUserId());
            } catch (JsonProcessingException e) {
              log.error(e.getMessage());
            }
          });
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  /**
   * 添加无限制的白名单
   *
   * @param results 结果集
   * @param name 角色名
   */
  private void addWhitelist(EnumMap<Result.Cause, Result> results, String name) {
    var userData = userDataService.getOneByName(name);
    if (userData != null) {
      // 更新角色名
      if (userData.getWhitelist() != 1) {
        userData.setWhitelist(1);
        userDataService.updateOne(userData);
        putResults(results, Result.success(name));
      } else {
        putResults(results, Result.whitelistAlreadyExists(name));
      }
      // 尝试删除multilogin.multilogin_cache_whitelist中的该条信息
      var cacheWhitelist = cacheWhitelistService.getOneByName(name);
      if (cacheWhitelist != null) {
        cacheWhitelistService.deleteOne(cacheWhitelist);
      }
    } else {
      // 查看multilogin.multilogin_cache_whitelist中是否有该条信息
      var cacheWhitelist = cacheWhitelistService.getOneByName(name);
      if (cacheWhitelist != null) {
        putResults(results, Result.whitelistAlreadyExists(name));
      } else {
        cacheWhitelistService.addOne(new CacheWhitelist(name));
        putResults(results, Result.success(name));
      }
    }
  }

  /**
   * 添加临时的白名单
   *
   * @param ctx 上下文环境
   * @param event 事件
   */
  private void addTempWhitelist(ChannelHandlerContext ctx, Event event) {
    // 获取所有有效角色名
    var legalNames = strUtil.getLegal(event.getMessage(), false);
    // 结果集
    var results = new EnumMap<Result.Cause, Result>(Result.Cause.class);
    for (var legalName : legalNames.get(0)) {
      addWhitelist(results, legalName);
    }
    results.forEach(
        (cause, result) -> {
          String message;
          var data = result.getData();
          switch (cause) {
            case SUCCESS -> message = strUtil.getString("添加 [{}] 条临时白名单 → {}", data.size(), data);
            case WHITELIST_ALREADY_EXISTS -> message = strUtil.getString("{} 已拥有白名单", data);
            default -> message = "未知分支";
          }
          try {
            apiUtil.sendLog(ctx, message);
          } catch (JsonProcessingException e) {
            log.error(e.getMessage());
          }
        });
  }

  /**
   * 查看白名单
   *
   * @param ctx 上下文环境
   * @param event 事件
   */
  private void checkWhitelist(ChannelHandlerContext ctx, Event event) {
    // 获取所有有效角色名
    var legalNames = strUtil.getLegal(event.getMessage(), false);
    var results = new EnumMap<Result.Cause, Result>(Result.Cause.class);
    for (var legalName : legalNames.get(0)) {
      // 查询multilogin.multilogin_user_data和multilogin.multilogin_cache_whitelist表
      var userData = userDataService.getOneByName(legalName);
      if (userData != null && userData.getWhitelist() == 1) {
        putResults(results, Result.success(legalName));
      } else if (cacheWhitelistService.getOneByName(legalName) != null) {
        putResults(results, Result.success(legalName));
      } else {
        putResults(results, Result.fail(legalName));
      }
    }
    try {
      // 获取所有群成员信息
      var groupMembers = apiUtil.getGroupMemberList(ctx, config.getListenGroupId());
      var legalNameUserIdMap = new HashMap<String, String>(8);
      for (var groupMember : groupMembers) {
        var names = strUtil.getLegal(groupMember.getCard(), false).get(0);
        for (var name : names) {
          if (results.get(Result.Cause.FAIL).getData().contains(name)) {
            legalNameUserIdMap.put(name, groupMember.getUserId());
          }
        }
      }
      results.forEach(
          (cause, result) -> {
            var data = result.getData();
            try {
              switch (cause) {
                case SUCCESS -> apiUtil.sendGroupReply(
                    ctx,
                    String.valueOf(event.getMessageId()),
                    strUtil.getString("{} 已拥有白名单", data),
                    event.getUserId());
                case FAIL -> {
                  apiUtil.sendGroupReply(
                      ctx,
                      String.valueOf(event.getMessageId()),
                      strUtil.getString("{} 未拥有白名单", data),
                      event.getUserId());
                  data.forEach(
                      name -> {
                        try {
                          // qq号不为空时才发送
                          if (legalNameUserIdMap.get(name) != null) {
                            apiUtil.sendAt(
                                ctx,
                                strUtil.splice(config.getNoWhitelist(), name),
                                legalNameUserIdMap.get(name));
                          }
                        } catch (JsonProcessingException e) {
                          log.error(e.getMessage());
                        }
                      });
                }
                default -> log.warn("未知分支");
              }
            } catch (JsonProcessingException e) {
              log.error(e.getMessage());
            }
          });
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private void putResults(EnumMap<Result.Cause, Result> results, Result result) {
    var cause = result.getCause();
    if (!results.containsKey(cause)) {
      results.put(cause, result);
    } else {
      result.getData().forEach(data -> results.get(cause).appendData(data));
    }
  }

  @Override
  public void memberDecrease(ChannelHandlerContext ctx, Event event) {
    if (!config.getListenGroupId().equals(event.getGroupId())) {
      return;
    }
    flushWhitelist(ctx);
  }

  @Override
  public void memberIncrease(ChannelHandlerContext ctx, Event event) {
    if (!config.getListenGroupId().equals(event.getGroupId())) {
      return;
    }
    flushWhitelist(ctx);
    log.info("[{}] 加入本群", event.getUserId());
    // @Q群管家
    try {
      apiUtil.atRobot(ctx);
    } catch (Exception e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    }
    try {
      var userId = event.getUserId();
      var groupMember = apiUtil.getGroupMemberInfo(ctx, userId);
      // 在群名片后跟个空格防止bug
      var autoCard = groupMember.getNickName() + " ";
      apiUtil.setGroupCard(ctx, userId, autoCard);
      var massage = strUtil.getString("自动为 \"{}\" [{}] 设置群名片", autoCard, event.getUserId());
      log.info(massage);
      apiUtil.sendLog(ctx, massage);
      // 在cache中添加该玩家
      cacheService.addOne(userId, autoCard);
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @Override
  public void memberMessage(ChannelHandlerContext ctx, Event event) {
    if (config.getListenGroupId().equals(event.getGroupId())) {
      listenGroupMessage(ctx, event);
    } else if (config.getReportGroupId().equals(event.getGroupId())) {
      reportGroupMessage(ctx, event);
    }
  }

  /**
   * 监听群消息事件
   *
   * @param ctx 上下文环境
   * @param event 事件
   */
  private void listenGroupMessage(ChannelHandlerContext ctx, Event event) {
    try {
      if (WHITELIST.equals(event.getMessage())) {
        // 白名单请求
        var message =
            strUtil.getString(
                "\"{}\" [{}] 请求更新白名单", event.getSender().getCard(), event.getUserId());
        log.info(message);
        apiUtil.sendLog(ctx, message);
        flushWhitelist(ctx);
        addWhitelist(ctx, event);
      } else if (event.getMessage().contains("有白名单吗")) {
        flushWhitelist(ctx);
        checkWhitelist(ctx, event);
      } else if ("黑名单".equals(event.getMessage())) {
        // 黑名单彩蛋
        apiUtil.sendGroupReply(
            ctx,
            String.valueOf(event.getMessageId()),
            strUtil.splice(config.getFakeBlacklist(), event.getUserId()),
            event.getUserId());
      }
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
    }
  }

  /**
   * 日志群消息事件
   *
   * @param ctx 上下文环境
   * @param event 事件
   */
  private void reportGroupMessage(ChannelHandlerContext ctx, Event event) {
    if ("刷新".equals(event.getMessage())) {
      flushWhitelist(ctx);
    } else {
      addTempWhitelist(ctx, event);
    }
  }

  @Override
  public void memberChangeCard(ChannelHandlerContext ctx, Event event) {
    if (!config.getListenGroupId().equals(event.getGroupId())) {
      return;
    }
    // 解决自动设置群名片乱码问题
    var userId = event.getUserId();
    var cardNew = event.getCardNew();
    var cardOld = cacheService.deleteOne(userId);
    if (cardOld.isEmpty() || !Objects.equals(cardOld, cardNew)) {
      // 不是自动设置，显示更新信息
      var message = strUtil.getString("\"{}\" [{}] 更新群名片 → \"{}\"", cardOld, userId, cardNew);
      log.info(message);
      try {
        apiUtil.sendLog(ctx, message);
      } catch (JsonProcessingException e) {
        log.error(e.getMessage());
      }
    }
    flushWhitelist(ctx);
  }

  @Override
  public void memberJoinRequest(ChannelHandlerContext ctx, Event event) {
    try {
      if (ADD.equals(event.getSubType())) {
        // 补充comment字段防止空指针
        var comment = event.getComment() == null ? "" : event.getComment();
        // 拼接答案与回答
        var question = "问题：" + config.getQuestion() + "\n答案：";
        var correctAnswer = question + config.getAnswer();
        var strangerAnswer = comment.substring(question.length());
        var userId = event.getUserId();
        // 查询用户等级
        var stranger = apiUtil.getStrangerInfo(ctx, userId);
        log.debug("flag：{}", event.getFlag());
        if (!comment.startsWith(question)) {
          // 邀请事件
          if (stranger.getLevel() >= config.getRequestLevel()) {
            // 等级符合需求直接放行
            log.info("同意 [{}] 入群请求", event.getUserId());
            apiUtil.setGroupAddRequest(ctx, event.getFlag(), event.getSubType(), true, "");
          }
          // 不处理拒绝事件
          return;
        }
        if (stranger.getLevel() < config.getRequestLevel()) {
          // 等级不足，拒绝
          log.warn("拒绝 [{}] 入群请求", userId);
          apiUtil.setGroupAddRequest(
              ctx,
              event.getFlag(),
              event.getSubType(),
              false,
              strUtil.getString("玩家等级需≥{}。可由群中成员邀请", config.getRequestLevel()));
          apiUtil.sendLog(
              ctx, strUtil.getString("拒绝 [{}] 入群请求，等级过低 → {}", userId, stranger.getLevel()));
        } else if (config.isEnableVerify() && !correctAnswer.equals(event.getComment())) {
          // 开启加群验证且答案错误
          log.warn("拒绝 [{}] 入群请求", userId);
          apiUtil.setGroupAddRequest(
              ctx, event.getFlag(), event.getSubType(), false, "回答错误，请输入：" + config.getAnswer());
          apiUtil.sendLog(ctx, strUtil.getString("拒绝 [{}] 入群请求，回答错误 → {}", userId, strangerAnswer));
        } else {
          log.info("同意 [{}] 入群请求", event.getUserId());
          apiUtil.setGroupAddRequest(ctx, event.getFlag(), event.getSubType(), true, "");
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    }
  }
}
