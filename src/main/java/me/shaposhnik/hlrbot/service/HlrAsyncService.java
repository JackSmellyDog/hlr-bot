package me.shaposhnik.hlrbot.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.SentHlr;
import org.springframework.scheduling.annotation.Async;

public interface HlrAsyncService extends HlrService {

  @Async
  CompletableFuture<Hlr> getHlrInfoAsync(SentHlr sentHlr, String token);

  @Async
  CompletableFuture<List<Hlr>> getHlrInfoListAsync(List<SentHlr> sentHlrList, String token);

}
