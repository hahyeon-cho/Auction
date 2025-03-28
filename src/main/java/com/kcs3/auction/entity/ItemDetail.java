package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "ItemDetail")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemDetailId", nullable = false)
    private Long itemDetailId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId", nullable = false)
    private Item item;

    @Column(nullable = false)
    private String itemDetailContent;

    @OneToMany(mappedBy = "itemDetail", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> images;

    @Builder
    public ItemDetail(String itemDetailContent) {
        this.itemDetailContent = itemDetailContent;
        this.images = new ArrayList<>();
    }

    public void setItem(Item item) { this.item = item; }

    public void addImage(ItemImage img) { images.add(img); }

    @Override
    public String toString() {
        return "ItemDetail {" + "id=" + getItemDetailId() +  // BaseEntity에서 상속받은 ID
            ", content='" + itemDetailContent + '\'' + ", item=" + (item != null ? "Item[id=" + item.getItemId() + "]"
            : "null") + '}';
    }
}
