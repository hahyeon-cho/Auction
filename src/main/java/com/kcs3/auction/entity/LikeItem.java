
package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data  // Lombok을 통해 기본 메소드 자동 생성
@Entity
@Getter
@Table(name ="LikeItem")
public class LikeItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likeId",nullable = false)
    private Long likeId;

    @ManyToOne( fetch = FetchType.LAZY) // 부모인자가 삭제되면 자동 삭제
    @JoinColumn(name ="userId")
    private User user;

    @ManyToOne( fetch = FetchType.LAZY) // 부모인자가 삭제되면 자동 삭제
    @JoinColumn (name="itemId")
    private Item item;

}