package me.shaposhnik.hlrbot.service;

import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface HlrAsyncService extends HlrService {

    @Async
    CompletableFuture<Hlr> getHlrInfoAsync(HlrId id, String token);

}
