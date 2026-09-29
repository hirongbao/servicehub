package com.shirongbao.hirongbaohub.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
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
            LocalDateTime eventDate = LocalDate.parse(dateStr).atStartOfDay();

            // 查找所有 next_holiday 类型的纪念日
            List<SiteAnniversary> autoHolidays = service.list(new LambdaQueryWrapper<SiteAnniversary>()
                    .eq(SiteAnniversary::getType, "next_holiday"));

            if (autoHolidays.isEmpty()) {
                // 如果没有，不需要自动创建，必须用户自己创建以设置背景图
                return;
            }

            for (SiteAnniversary anniversary : autoHolidays) {
                // 如果假期信息有变，更新
                if (!localName.equals(anniversary.getTitle()) || !eventDate.equals(anniversary.getEventDate())) {
                    anniversary.setTitle(localName);
                    anniversary.setEventDate(eventDate);
                    service.updateById(anniversary);
                    log.info("更新下一个自动假期成功: {} - {}", localName, dateStr);
                }
            }

        } catch (Exception e) {
            log.error("定时获取下一次假期失败", e);
        }
    }
}
