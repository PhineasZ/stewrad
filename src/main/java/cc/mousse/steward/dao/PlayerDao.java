package cc.mousse.steward.dao;

import cc.mousse.steward.domain.Player;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author PhineasZ
 */
@Mapper
public interface PlayerDao extends BaseMapper<Player> {}
