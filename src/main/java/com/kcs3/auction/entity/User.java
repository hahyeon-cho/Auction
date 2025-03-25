package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String userNickname;

    @Column(nullable = false, length = 255, unique = true)
    private String userEmail;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int userPoint;

    @ElementCollection
    @CollectionTable(name = "user_cookies", joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "cookie")
    private List<Long> cookies = new ArrayList<>();

    @Builder
    public User(String userNickname, String userEmail, int userPoint) {
        this.userNickname = userNickname;
        this.userEmail = userEmail;
        this.userPoint = userPoint;
    }
}