package com.xidian.activities.mapper;

import com.xidian.activities.bean.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivityMapper {
    Activity getById(@Param("id") Long id);
    Activity getByName(@Param("activityName") String activityName);
    void save(Activity activity);
    void update(Activity activity);
    void deleteById(@Param("id") Long id);
}
