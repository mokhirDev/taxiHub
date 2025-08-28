package com.mokhir.dev.telegram.bot.taxi.hub.entity;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ClientTypeEnum;
import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.LocaleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_state")
public class UserState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "locale")
    @Enumerated(EnumType.STRING)
    private LocaleEnum locale;
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private ClientTypeEnum role;
    @Column(name = "current_page_code")
    private String currentPageCode;
    @Column(name = "last_message_id")
    private Integer lastMessageId;
}