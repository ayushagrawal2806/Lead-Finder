package com.youtube.leadfinder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.leadfinder.dto.ChannelResponse;
import com.youtube.leadfinder.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final WebClient webClient =
            WebClient.create();

    public SearchResponse searchChannels(

            String keyword,

            Long minSubscribers,

            Long maxSubscribers,

            int limit,

            String pageToken
    ) {

        List<ChannelResponse> results =
                new ArrayList<>();

        try {

            ObjectMapper mapper =
                    new ObjectMapper();

            // UNIQUE CHANNEL IDS

            Set<String> channelIds =
                    new HashSet<>();

            // UNIQUE EMAILS

            Set<String> processedEmails =
                    new HashSet<>();

            String nextPageToken =
                    pageToken == null
                            ? ""
                            : pageToken;

            // KEEP FETCHING UNTIL LIMIT REACHED

            while (results.size() < limit) {

                String searchUrl =
                        "https://www.googleapis.com/youtube/v3/search"
                                + "?part=snippet"
                                + "&type=channel"
                                + "&maxResults=50"
                                + "&q=" + keyword
                                + "&pageToken=" + nextPageToken
                                + "&key=" + apiKey;

                String searchResponse =
                        webClient.get()
                                .uri(searchUrl)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                JsonNode root =
                        mapper.readTree(searchResponse);

                JsonNode items =
                        root.get("items");

                // STORE UNIQUE CHANNEL IDS

                for (JsonNode item : items) {

                    String channelId =
                            item.get("id")
                                    .get("channelId")
                                    .asText();

                    channelIds.add(channelId);
                }

                // NEXT PAGE TOKEN

                if (root.has("nextPageToken")) {

                    nextPageToken =
                            root.get("nextPageToken")
                                    .asText();

                } else {

                    nextPageToken = null;

                    break;
                }

                // CONVERT SET TO LIST

                List<String> uniqueChannelIds =
                        new ArrayList<>(channelIds);

                // FETCH CHANNEL DETAILS IN BATCHES

                for (int i = 0;
                     i < uniqueChannelIds.size();
                     i += 50) {

                    List<String> batch =
                            uniqueChannelIds.subList(
                                    i,
                                    Math.min(
                                            i + 50,
                                            uniqueChannelIds.size()
                                    )
                            );

                    String ids =
                            String.join(",", batch);

                    String detailsUrl =
                            "https://www.googleapis.com/youtube/v3/channels"
                                    + "?part=snippet,statistics"
                                    + "&fields=items(id,snippet(title,description),statistics(subscriberCount))"
                                    + "&id=" + ids
                                    + "&key=" + apiKey;

                    String detailsResponse =
                            webClient.get()
                                    .uri(detailsUrl)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                    JsonNode detailsRoot =
                            mapper.readTree(detailsResponse);

                    JsonNode channels =
                            detailsRoot.get("items");

                    for (JsonNode channel : channels) {

                        String channelName =
                                channel.get("snippet")
                                        .get("title")
                                        .asText();

                        String description =
                                channel.get("snippet")
                                        .get("description")
                                        .asText();

                        long subscribers =
                                channel.get("statistics")
                                        .get("subscriberCount")
                                        .asLong();

                        // MIN FILTER

                        if (minSubscribers != null
                                && subscribers < minSubscribers) {

                            continue;
                        }

                        // MAX FILTER

                        if (maxSubscribers != null
                                && subscribers > maxSubscribers) {

                            continue;
                        }

                        // EMAIL EXTRACTION

                        String email =
                                extractEmail(description);

                        // ONLY CHANNELS WITH EMAIL

                        if (email == null
                                || email.isBlank()) {

                            continue;
                        }

                        // REMOVE DUPLICATE EMAILS

                        if (processedEmails.contains(email)) {

                            continue;
                        }

                        processedEmails.add(email);

                        // ADD RESULT

                        results.add(

                                new ChannelResponse(

                                        channelName,

                                        subscribers,

                                        email
                                )
                        );

                        // STOP WHEN LIMIT REACHED

                        if (results.size() >= limit) {

                            break;
                        }
                    }

                    if (results.size() >= limit) {

                        break;
                    }
                }
            }

            return new SearchResponse(
                    results,
                    nextPageToken
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        return new SearchResponse(
                new ArrayList<>(),
                null
        );
    }

    // EMAIL EXTRACTOR

    private String extractEmail(String text) {

        if (text == null) {

            return null;
        }

        String regex =
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

        Pattern pattern =
                Pattern.compile(regex);

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {

            return matcher.group();
        }

        return null;
    }
}