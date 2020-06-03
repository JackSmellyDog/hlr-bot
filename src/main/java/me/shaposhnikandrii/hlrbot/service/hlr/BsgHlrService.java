package me.shaposhnikandrii.hlrbot.service.hlr;

import me.shaposhnikandrii.hlrbot.httpclient.BsgApiHttpClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrService {
  private final BsgApiHttpClient bsgApiHttpClient;




}