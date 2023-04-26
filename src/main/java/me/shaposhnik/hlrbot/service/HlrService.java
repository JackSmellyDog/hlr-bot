package me.shaposhnik.hlrbot.service;

import java.util.List;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.model.SentHlr;

public interface HlrService {

  SentHlr sendHlr(Phone phone, String token);

  List<SentHlr> sendHlrs(List<Phone> phones, String token);

  Hlr getHlrInfo(SentHlr sentHlr, String token);

  Hlr getHlrInfoByProviderId(String providerId, String token);

}
