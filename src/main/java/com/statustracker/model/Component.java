package com.statustracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Component {
    private String id;
    private String name;
    private String status;
    private String description;
    private String groupId;
    private int position;
}
