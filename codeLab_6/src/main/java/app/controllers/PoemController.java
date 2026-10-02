package app.controllers;

import app.daos.PoemDAO;
import app.dtos.PoemDTO;
import app.entities.Poem;
import app.mappers.PoemMapper;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PoemController {

    private final PoemDAO poemDAO;

    public PoemController(PoemDAO poemDAO) {
        this.poemDAO = poemDAO;
    }

    public void getPoems(Context ctx) {
        List<Poem> poems = poemDAO.getPoems();

        List<PoemDTO> poemDTOs =
                PoemMapper.toDTOList(poems);

        ctx.status(HttpStatus.OK);
        ctx.json(poemDTOs);
    }

    public void createPoems(Context ctx) {
        PoemDTO[] poemDTOs =
                ctx.bodyAsClass(PoemDTO[].class);

        List<Poem> poems = Arrays.stream(poemDTOs)
                .map(PoemMapper::toEntity)
                .toList();

        List<Poem> createdPoems =
                poemDAO.createFromList(poems);

        ctx.status(HttpStatus.CREATED);
        ctx.json(PoemMapper.toDTOList(createdPoems));
    }

    public void createPoem(Context ctx) {
        PoemDTO poemDTO =
                ctx.bodyAsClass(PoemDTO.class);

        Poem poem =
                PoemMapper.toEntity(poemDTO);

        Poem createdPoem =
                poemDAO.create(poem);

        ctx.status(HttpStatus.CREATED);
        ctx.json(PoemMapper.toDTO(createdPoem));
    }

    public void delete(Context ctx) {
        int id = getId(ctx);

        boolean deleted = poemDAO.delete(id);

        if (deleted) {
            ctx.status(HttpStatus.OK);
            ctx.json(Map.of(
                    "message", "Poem deleted",
                    "id", id
            ));
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(Map.of(
                    "message", "No poem found with id",
                    "id", id
            ));
        }
    }

    public void update(Context ctx) {
        int id = getId(ctx);

        PoemDTO poemDTO =
                ctx.bodyAsClass(PoemDTO.class);

        Poem poem =
                PoemMapper.toEntity(poemDTO);

        Poem updatedPoem =
                poemDAO.update(id, poem);

        if (updatedPoem != null) {
            ctx.status(HttpStatus.OK);
            ctx.json(PoemMapper.toDTO(updatedPoem));
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(Map.of(
                    "message", "No poem found with id",
                    "id", id
            ));
        }
    }

    public void getById(Context ctx) {
        int id = getId(ctx);

        Poem poem =
                poemDAO.getPoemById(id);

        if (poem != null) {
            ctx.status(HttpStatus.OK);
            ctx.json(PoemMapper.toDTO(poem));
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(Map.of(
                    "message", "No poem found with id",
                    "id", id
            ));
        }
    }

    private int getId(Context ctx) {
        return ctx.pathParamAsClass(
                "id",
                Integer.class
        ).get();
    }
}
