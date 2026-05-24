package com.youtube.leadfinder.controller;

import com.youtube.leadfinder.dto.SearchResponse;
import com.youtube.leadfinder.service.YouTubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final YouTubeService youTubeService;

    @GetMapping("/search")
    public SearchResponse searchChannels(

            @RequestParam String keyword,

            @RequestParam(required = false)
            Long minSubscribers,

            @RequestParam(required = false)
            Long maxSubscribers,

            @RequestParam(defaultValue = "100")
            int limit,

            @RequestParam(required = false)
            String pageToken
    ) {

        // LIMIT PROTECTION
//
//        if (limit > 100) {
//            limit = 100;
//        }

        return youTubeService.searchChannels(

                keyword,

                minSubscribers,

                maxSubscribers,

                limit,

                pageToken
        );
    }
}