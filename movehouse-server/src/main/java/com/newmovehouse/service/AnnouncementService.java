package com.newmovehouse.service;

import com.newmovehouse.entity.Entities;
import com.newmovehouse.mapper.AppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统公告查询：按受众（用户端/司机端）过滤已启用公告。
 */
@Service
public class AnnouncementService {
    @Autowired
    private AppMapper mapper;

    /**
     * @param audience 受众标识，与库中 {@code audience} 字段一致
     * @return 公告列表
     */
    public List<Entities.Announcement> listForAudience(String audience) {
        return mapper.listAnnouncementsByAudience(audience);
    }
}
