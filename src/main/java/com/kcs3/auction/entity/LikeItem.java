package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LikeItem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likeId", nullable = false)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY) // 부모인자가 삭제되면 자동 삭제
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 부모인자가 삭제되면 자동 삭제
    @JoinColumn(name = "itemId")
    private Item item;

    @Builder
    public LikeItem(User user, Item item) {
        this.user = user;
        this.item = item;
    }
}