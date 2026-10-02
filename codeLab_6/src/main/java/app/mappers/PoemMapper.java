package app.mappers;

import app.dtos.PoemDTO;
import app.entities.Poem;

import java.util.List;

public class PoemMapper {

    private PoemMapper() {
    }

    public static PoemDTO toDTO(Poem poem) {
        return new PoemDTO(
            poem.getId(),
            poem.getTitle(),
            poem.getPoem(),
            poem.getStyle()
        );
    }

    public static Poem toEntity(PoemDTO dto) {
        return new Poem(
            dto.id(),
            dto.title(),
            dto.poem(),
            dto.style()
        );
    }

    public static List<PoemDTO> toDTOList(List<Poem> poems) {
        return poems.stream()
            .map(PoemMapper::toDTO)
            .toList();
    }
}