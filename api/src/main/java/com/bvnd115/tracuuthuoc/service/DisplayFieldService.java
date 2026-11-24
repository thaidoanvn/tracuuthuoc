package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.DisplayField;
import com.bvnd115.tracuuthuoc.Messages;
import com.bvnd115.tracuuthuoc.DisplayFields;
import com.bvnd115.tracuuthuoc.exception.ApiException;
import com.bvnd115.tracuuthuoc.repository.DisplayFieldRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisplayFieldService {

    private final DisplayFieldRepository repository;
    private final Messages messages;

    public DisplayFieldService(DisplayFieldRepository repository, Messages messages) {
        this.repository = repository;
        this.messages = messages;
    }

    @Transactional
    public Map<String, Boolean> current() {
        seed();
        Map<String, Boolean> fields = new LinkedHashMap<>();
        Map<String, DisplayField> stored = new LinkedHashMap<>();
        repository.findAll().forEach(field -> stored.put(field.getFieldKey(), field));
        for (String key : DisplayFields.KEYS) {
            DisplayField field = stored.get(key);
            fields.put(key, field == null || field.isVisible());
        }
        return fields;
    }

    @Transactional
    public Map<String, Boolean> update(Map<String, Boolean> requested) {
        if (requested == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, messages.get(Messages.DISPLAY_FIELDS_MISSING));
        }
        for (String key : requested.keySet()) {
            if (!DisplayFields.known(key)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, messages.get(Messages.DISPLAY_FIELD_INVALID));
            }
        }
        seed();
        for (Map.Entry<String, Boolean> entry : requested.entrySet()) {
            DisplayField field = repository.findById(entry.getKey())
                    .orElseGet(() -> new DisplayField(entry.getKey(), true));
            field.setVisible(Boolean.TRUE.equals(entry.getValue()));
            repository.save(field);
        }
        return current();
    }

    private void seed() {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(DisplayFields.KEYS.stream().map(key -> new DisplayField(key, true)).toList());
    }
}
