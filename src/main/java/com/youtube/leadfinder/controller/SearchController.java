package com.youtube.leadfinder.controller;

import com.youtube.leadfinder.dto.ChannelResponse;
import com.youtube.leadfinder.service.YouTubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final YouTubeService youTubeService;

    @GetMapping("/search")
    public List<ChannelResponse> searchChannels(

            @RequestParam String keyword,

            @RequestParam(required = false)
            Long minSubscribers,

            @RequestParam(required = false)
            Long maxSubscribers,

            @RequestParam(defaultValue = "100")
            int limit
    ) {

        return youTubeService.searchChannels(

                keyword,

                minSubscribers,

                maxSubscribers,

                limit
        );
    }
}