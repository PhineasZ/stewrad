package cc.mousse.steward.service;

import cc.mousse.steward.domain.Player;

import java.util.List;

/**
 * @author PhineasZ
 */
public interface PlayerService {

  /**
   * 获取所有BlessingSkin角色信息
   * @return 角色信息列表
   */
  List<Player> getAll();

  /**
   * 根据角色名获取角色信息
   *
   * @param name 角色名
   * @return 角色信息
   */
  Player getOneByName(String name);
}
