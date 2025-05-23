package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String nickname;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer userPoint;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemLike> itemLikes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_cookies", joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "cookie")
    private final List<Long> cookies = new ArrayList<>();

    @Builder
    public User(String nickname, String email, Integer userPoint) {
        this.nickname = nickname;
        this.email = email;
        this.userPoint = userPoint;
    }

    // === Add & Update ===
    public void addLike(ItemLike itemLike) { this.itemLikes.add(itemLike); }
}
