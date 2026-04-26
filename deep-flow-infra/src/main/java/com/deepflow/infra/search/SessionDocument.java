package com.deepflow.infra.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "session_v1")
@Setting(settingPath = "es/session-settings.json")
public class SessionDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long crewId;

    @Field(type = FieldType.Long)
    private Long ownerUserId;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String summary;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private String sharedAt;

    @Field(type = FieldType.Integer)
    private int reactionCount;

    @Field(type = FieldType.Integer)
    private int commentCount;
}
