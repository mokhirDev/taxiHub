package com.mokhir.dev.telegram.bot.taxi.hub.entity;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.enums.ClientTypeEnum;
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
    private String locale;
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private ClientTypeEnum role;
    @Column(name = "has_phone")
    private boolean hasPhone;
    @Column(name = "has_from_location")
    private boolean hasFromLocation;
    @Column(name = "has_to_location")
    private boolean hasToLocation;
    @Column(name = "has_date")
    private boolean hasDate;
    @Column(name = "has_seats")
    private boolean hasSeats;
    @Column(name = "order_complete")
    private boolean orderComplete;
    @Column(name = "current_page_code")
    private String currentPageCode;
    @Column(name = "last_message_id")
    private Integer lastMessageId;
    @OneToMany(mappedBy = "userState", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders;
}