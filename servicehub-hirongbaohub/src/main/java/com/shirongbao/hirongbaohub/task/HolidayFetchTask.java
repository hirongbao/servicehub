package com.shirongbao.hirongbaohub.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
import com.shirongbao.hirongbaohub.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayFetchTask {

    private final SiteAnniversaryService service;
    private final SiteUserService siteUserService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 1 0 * * ?") // 每天凌晨 00:01 执行
    public void fetchNextHoliday() {
        try {
            // 获取最新公休假
            ResponseEntity<List> response = restTemplate.getForEntity("https://date.nager.at/api/v3/NextPublicHolidays/CN", List.class);
            List<Map<String, Object>> holidays = response.getBody();
            if (holidays == null || holidays.isEmpty()) return;

            Map<String, Object> nextHoliday = holidays.get(0);
            String localName = (String) nextHoliday.get("localName");
            String dateStr = (String) nextHoliday.get("date"); // YYYY-MM-DD
            LocalDate eventDate = LocalDate.parse(dateStr);

            // 根据日期和标题去重（为了防止同一天有不同记录，主要按日期去重即可）
            boolean exists = service.count(new LambdaQueryWrapper<SiteAnniversary>()
                    .eq(SiteAnniversary::getEventDate, eventDate)) > 0;

            if (!exists) {
                // 如果不存在，则新增一条记录，作为历史留存
                SiteAnniversary holiday = new SiteAnniversary();
                holiday.setTitle(localName);
                holiday.setEventDate(eventDate);
                holiday.setType("next_holiday");
                holiday.setIcon("Plane"); // 默认图标
                holiday.setCoverUrl(""); // 默认为空，用户自己上传
                holiday.setIsEnabled(true);
                holiday.setSortOrder(0);
                holiday.setUserId(siteUserService.getAdminUserId());
                
                service.save(holiday);
                log.info("自动创建了新的假期记录: {} - {}", localName, dateStr);
            }

        } catch (Exception e) {
            log.error("定时获取下一次假期失败", e);
        }
    }
}
