package com.bvnd115.tracuuthuoc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "display_field")
public class DisplayField {

    @Id
    @Column(length = 64)
    private String fieldKey;

    @Column(nullable = false)
    private boolean visible = true;

    public DisplayField() {
    }

    public DisplayField(String fieldKey, boolean visible) {
        this.fieldKey = fieldKey;
        this.visible = visible;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
