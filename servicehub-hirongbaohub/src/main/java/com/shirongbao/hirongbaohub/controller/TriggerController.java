package com.shirongbao.hirongbaohub.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shirongbao.hirongbaohub.task.HolidayFetchTask;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
public class TriggerController {
    private final HolidayFetchTask task;
    @GetMapping(" /api/trigger-holiday)
