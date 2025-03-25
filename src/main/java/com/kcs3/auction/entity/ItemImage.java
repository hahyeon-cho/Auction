package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "ItemImage")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemImage", nullable = false)
    private Long itemImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemDetailId")
    private ItemDetail itemDetail;

    @Column(name = "url", nullable = false)
    private String url;

    @Builder
    public ItemImage(ItemDetail itemDetail, String url) {
        this.itemDetail = itemDetail;
        this.url = url;
    }

    @Override
    public String toString() {
        return "ItemImage{" +
                "id=" + itemImage +  // BaseEntity에서 상속받은 ID
                ", url='" + url + '\'' +
                ", itemDetail=" + (itemDetail != null ? "ItemDetail[id=" + itemDetail.getItemDetailId() + "]" : "null") +
                '}';
    }
}
