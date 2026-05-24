package com.youtube.leadfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelResponse {

    private String channelName;

    private long subscribers;

    private String email;
}