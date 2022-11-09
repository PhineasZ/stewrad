package cc.mousse.steward.dao;

import cc.mousse.steward.domain.UserData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author PhineasZ
 */
@Mapper
public interface UserDataDao extends BaseMapper<UserData> {}
