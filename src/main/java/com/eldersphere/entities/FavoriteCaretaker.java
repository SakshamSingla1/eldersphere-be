package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_caretakers")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCaretaker extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_user_id", nullable = false)
    private Long familyUserId;

    @Column(name = "caretaker_id", nullable = false)
    private Long caretakerId;
}
