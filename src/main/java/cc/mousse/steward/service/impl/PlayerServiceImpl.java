package cc.mousse.steward.service.impl;

import cc.mousse.steward.dao.PlayerDao;
import cc.mousse.steward.domain.Player;
import cc.mousse.steward.service.PlayerService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author PhineasZ
 */
@Service
@DS("blessingskin")
public class PlayerServiceImpl implements PlayerService {

  @Resource private PlayerDao playersDao;

  @Override
  public List<Player> getAll() {
    return playersDao.selectList(null);
  }

  @Override
  public Player getOneByName(String name) {
    return playersDao.selectOne(new QueryWrapper<Player>().eq("name", name));
  }
}
