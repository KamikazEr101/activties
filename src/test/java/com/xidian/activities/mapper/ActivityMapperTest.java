package com.xidian.activities.mapper;
import java.time.LocalDateTime;

import com.xidian.activities.bean.Activity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ActivityMapperTest {
    @Autowired
    ActivityMapper activityMapper;

    @Test
    void testSave() {
        Activity activity = new Activity();
        activity.setActivityName("testSave");
        activity.setActivityDescription("");
        activity.setActivityType("");
        activity.setActivityStatus(0);
        activity.setStartTime(LocalDateTime.now());
        activity.setEndTime(LocalDateTime.now());
        activity.setLocation("");
        activity.setOrganizer("");
        activity.setContactPerson("");
        activity.setContactPhone("");
        activity.setContactEmail("");
        activity.setPosterUrl("");
        activity.setMaxParticipants(0);
        activity.setCurrentParticipants(0);
        activity.setRegistrationStartTime(LocalDateTime.now());
        activity.setRegistrationEndTime(LocalDateTime.now());
        activity.setRegistrationRequirement("");
        activity.setCheckInStartTime(LocalDateTime.now());
        activity.setCheckInEndTime(LocalDateTime.now());
        activity.setPublishTime(LocalDateTime.now());
        activity.setCreatorId(0L);
        activity.setIsDeleted(false);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        activity.setActivityName("testSave");

        activityMapper.save(activity);
        Activity activityInsert = activityMapper.getById(activity.getId());
        System.out.println(activityInsert);
    }

    @Test
    void testGetById() {
        Activity activity = activityMapper.getById(activityMapper.getByName("testSave").getId());
        System.out.println(activity);
    }


    @Test
    void testUpdate() {
        Activity activity = activityMapper.getByName("testSave");
        System.out.println(activity);
        activity.setActivityName("testUpdate");
        activityMapper.update(activity);
        System.out.println(activityMapper.getByName("testUpdate"));
    }

    @Test
    void testDelete() {
        Activity activity = activityMapper.getByName("testUpdate");
        activityMapper.deleteById(activity.getId());
    }
}
