package me.shaposhnik.hlrbot.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "allowed_telegram_id")
@Getter
@Setter
public class AllowedTelegramId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String telegramId;

}
