package cc.mousse.steward.service.impl;

import cc.mousse.steward.common.Config;
import cc.mousse.steward.common.Event;
import cc.mousse.steward.common.Result;
import cc.mousse.steward.domain.CacheWhitelist;
import cc.mousse.steward.domain.GroupMember;
import cc.mousse.steward.domain.UserData;
import cc.mousse.steward.service.CacheWhitelistService;
import cc.mousse.steward.service.GroupService;
import cc.mousse.steward.service.PlayerService;
import cc.mousse.steward.service.UserDataService;
import cc.mousse.steward.utils.ApiUtil;
import cc.mousse.steward.utils.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

  @Resource private PlayerService playerService;

  @Resource private UserDataService userDataService;

  @Resource private CacheWhitelistService cacheWhitelistService;

  /**
   * 清理白名单
   *
   * @param ctx 上下文环境
   */
  private void flushWhitelist(ChannelHandlerContext ctx) {
    log.info("开始清理白名单");
    try {
      // 垃圾桶
      val bin = new HashSet<String>();
      // 获取所有群成员信息
      val groupMembers = apiUtil.getGroupMemberList(ctx, config.getListenGroupId());
      // 获取所有有效群名片
      val legalCards = new HashSet<String>();
      groupMembers.forEach(
          groupMember -> legalCards.addAll(strUtil.getLegal(groupMember.getCard()).get(0)));
      // blessingsink.player角色集合
      val players = new HashSet<String>();
      playerService.getAll().forEach(player -> players.add(player.getName()));
      // 遍历所有有效群名片
      // 若blessingsink.player中不存在则过滤
      legalCards.removeIf(name -> !players.contains(name));
      // 清理multilogin.multilogin_cache_whitelist表
      for (CacheWhitelist cacheWhitelist : cacheWhitelistService.getAll()) {
        val name = cacheWhitelist.getSign();
        // 若有效群名片不存在该缓存则清除
        if (!legalCards.contains(name)) {
          cacheWhitelistService.deleteOne(cacheWhitelist);
          bin.add(name);
        }
      }
      // 清理multilogin.multilogin_user_data表
      for (UserData userData : userDataService.getAll()) {
        val name = userData.getCurrentName();
        // 若有效群名片不存在该缓存则更新
        if (!legalCards.contains(name) && userData.getWhitelist() == 1) {
          userData.setWhitelist(0);
          userDataService.updateOne(userData);
          bin.add(name);
        }
      }
      if (!bin.isEmpty()) {
        val message = "清理[" + bin.size() + "]条白名单：" + bin;
        log.info(message);
        apiUtil.sendLog(ctx, message);
      }
      log.info("完成清理白名单");
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
      val userId = event.getUserId();
      // 结果集
      val results = new EnumMap<Result.Cause, Result>(Result.Cause.class);
      // 获取玩家群名片
      val groupMember = apiUtil.getGroupMemberInfo(ctx, userId);
      if (groupMember == null) {
        log.error("群成员信息获取失败");
        return;
      }
      // 获取所有有效角色名
      val names = strUtil.getLegal(groupMember.getCard());
      // 超出数量限制的角色名
      for (String illegalName : names.get(1)) {
        putResults(results, Result.outOfLimit(illegalName));
      }
      val legalNames = names.get(0);
      if (legalNames != null && legalNames.size() != 0) {
        // 存在有效角色名
        // 处理每个角色名
        for (String legalName : legalNames) {
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
      log.info("返回结果");
      results.forEach(
          (cause, result) -> {
            String message;
            val data = result.getData();
            switch (cause) {
              case SUCCESS -> {
                try {
                  apiUtil.sendLog(ctx, "添加[" + data.size() + "]条白名单：" + data);
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                message = strUtil.splice(config.getSuccess(), data);
              }
              case PLAYER_NOT_FOUND -> message = strUtil.splice(config.getPlayerNotFound(), data);
              case WHITELIST_ALREADY_EXISTS -> message =
                  strUtil.splice(config.getWhitelistAlreadyExists(), data);
              case NO_LEGAL_NAME -> message = config.getNoLegalName();
              case OUT_OF_LIMIT -> {
                val list = new ArrayList<>();
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
              throw new RuntimeException(e);
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
    val userData = userDataService.getOneByName(name);
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
      val cacheWhitelist = cacheWhitelistService.getOneByName(name);
      if (cacheWhitelist != null) {
        cacheWhitelistService.deleteOne(cacheWhitelist);
      }
    } else {
      // 查看multilogin.multilogin_cache_whitelist中是否有该条信息
      val cacheWhitelist = cacheWhitelistService.getOneByName(name);
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
    val legalNames = strUtil.getLegal(event.getMessage(), false);
    // 结果集
    val results = new EnumMap<Result.Cause, Result>(Result.Cause.class);
    for (String legalName : legalNames.get(0)) {
      addWhitelist(results, legalName);
    }
    results.forEach(
        (cause, result) -> {
          String message;
          val data = result.getData();
          switch (cause) {
            case SUCCESS -> message = "添加[" + data.size() + "]条临时白名单：" + data;
            case WHITELIST_ALREADY_EXISTS -> message = data + "已拥有白名单";
            default -> message = "未知分支";
          }
          try {
            apiUtil.sendLog(ctx, message);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
    val legalNames = strUtil.getLegal(event.getMessage(), false);
    val results = new EnumMap<Result.Cause, Result>(Result.Cause.class);
    for (String legalName : legalNames.get(0)) {
      // 查询multilogin.multilogin_user_data和multilogin.multilogin_cache_whitelist表
      val userData = userDataService.getOneByName(legalName);
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
      val groupMembers = apiUtil.getGroupMemberList(ctx, config.getListenGroupId());
      val legalNameUserIdMap = new HashMap<String, String>(8);
      for (GroupMember groupMember : groupMembers) {
        val names = strUtil.getLegal(groupMember.getCard(), false).get(0);
        for (String name : names) {
          if (results.get(Result.Cause.FAIL).getData().contains(name)) {
            legalNameUserIdMap.put(name, groupMember.getUserId());
          }
        }
      }
      results.forEach(
          (cause, result) -> {
            val data = result.getData();
            try {
              switch (cause) {
                case SUCCESS -> apiUtil.sendGroupReply(
                    ctx, String.valueOf(event.getMessageId()), data + "已拥有白名单", event.getUserId());
                case FAIL -> {
                  apiUtil.sendGroupReply(
                      ctx,
                      String.valueOf(event.getMessageId()),
                      data + "未拥有白名单",
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
                          throw new RuntimeException(e);
                        }
                      });
                }
                default -> log.warn("未知分支");
              }
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
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
    val cause = result.getCause();
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
    log.info("[{}]加入本群", event.getUserId());
    // @Q群管家
    try {
      apiUtil.atRobot(ctx);
    } catch (Exception e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    }
    // TODO 改群名片有Bug
    /*
       try {
         val userId = event.getUserId();
         val groupMember = apiUtil.getGroupMemberInfo(ctx, userId);
         apiUtil.setGroupCard(ctx, userId, groupMember.getNickName());
         log.info("设置[{}]群名片：{}", event.getUserId(), apiUtil.getGroupMemberInfo(ctx,
    userId).getCard());
       } catch (InterruptedException e) {
         log.error(e.getMessage());
         Thread.currentThread().interrupt();
       } catch (Exception e) {
         log.error(e.getMessage());
       }
       */
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
        String message = "[" + event.getUserId() + "]请求更新白名单：" + event.getSender().getCard();
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
    String userId = event.getUserId();
    String cardNew = event.getCardNew();
    String cardOld = event.getCardOld();
    String message = cardOld + " [" + userId + "] 更新群名片：" + cardNew;
    log.info(message);
    try {
      apiUtil.sendLog(ctx, message);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
    }
    flushWhitelist(ctx);
  }

  @Override
  public void memberJoinRequest(ChannelHandlerContext ctx, Event event) {
    try {
      if (ADD.equals(event.getSubType())) {
        // 补充comment字段防止空指针
        val comment = event.getComment() == null ? "" : event.getComment();
        // 拼接答案与回答
        val question = "问题：" + config.getQuestion() + "\n答案：";
        if (!comment.startsWith(question)) {
          // 不处理群成员邀请事件
          return;
        }
        val correctAnswer = question + config.getAnswer();
        val strangerAnswer = comment.substring(question.length());
        val userId = event.getUserId();
        // 查询用户等级
        val stranger = apiUtil.getStrangerInfo(ctx, userId);
        log.info("flag：{}", event.getFlag());
        if (stranger.getLevel() < config.getRequestLevel()) {
          // 等级不足，拒绝
          log.warn("不同意入群请求：{}", userId);
          apiUtil.setGroupAddRequest(
              ctx,
              event.getFlag(),
              event.getSubType(),
              false,
              "玩家等级需≥" + config.getRequestLevel() + "。可由群中成员邀请");
          apiUtil.sendLog(ctx, "拒绝[" + userId + "]入群请求，等级：" + stranger.getLevel());
        } else if (config.isEnableVerify() && !correctAnswer.equals(event.getComment())) {
          // 开启加群验证且答案错误
          log.warn("不同意入群请求：{}", userId);
          apiUtil.setGroupAddRequest(
              ctx, event.getFlag(), event.getSubType(), false, "回答错误，请输入：" + config.getAnswer());
          apiUtil.sendLog(ctx, "拒绝[" + userId + "]入群请求，回答：" + strangerAnswer);
        } else {
          log.info("同意入群请求：{}", event.getUserId());
          apiUtil.setGroupAddRequest(ctx, event.getFlag(), event.getSubType(), true, "");
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void groupRecall(ChannelHandlerContext ctx, Event event) {}

  @Override
  public void groupUpload(ChannelHandlerContext ctx, Event event) {}
}
