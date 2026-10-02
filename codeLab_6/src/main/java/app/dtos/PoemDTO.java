package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PoemDTO(
    Integer id,
    String title,
    String poem,
    String style
) {

    public PoemDTO(String title, String poem, String style) {
        this(null, title, poem, style);
    }
}