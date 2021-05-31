package me.shaposhnik.hlrbot.service;

import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.HlrIdPhonePair;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HlrAsyncService extends HlrService {

    @Async
    CompletableFuture<Hlr> getHlrInfoAsync(HlrId hlrId, String token);

    @Async
    CompletableFuture<List<Hlr>> getHlrInfoListAsync(List<HlrIdPhonePair> hlrIdPhonePairs, String token);

}
