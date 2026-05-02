package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开公告列表（按受众区分用户端/司机端）。
 */
@RestController
@RequestMapping("/api/common/announcements")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    /** 按受众查询启用中的公告 */
    @GetMapping
    public ApiResponse<List<Entities.Announcement>> list(@RequestParam String audience) {
        return ApiResponse.ok(announcementService.listForAudience(audience));
    }
}
