package app.controllers;

import app.daos.PoemDAO;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public class PoemController {

    private final PoemDAO dao;

    public PoemController(PoemDAO dao) {
        this.dao = dao;
    }

    public void readAll(Context ctx) {
        ctx.status(200).json(dao.readAll());
    }

    public void create(Context ctx) {
        app.dtos.PoemDTO created = dao.create(validateEntity(ctx));
        ctx.status(201).json(created);
    }

    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
            .check(value -> value > 0, "Id must be positive")
            .get();

        app.dtos.PoemDTO poem = dao.read(id);

        if (poem == null) {
            throw new NotFoundResponse("Poem not found");
        }

        ctx.status(200).json(poem);
    }

    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        app.dtos.PoemDTO updated = dao.update(id, validateEntity(ctx));

        if (updated == null) {
            throw new NotFoundResponse("Poem not found");
        }

        ctx.status(200).json(updated);
    }

    private app.dtos.PoemDTO validateEntity(Context ctx) {
        return ctx.bodyValidator(app.dtos.PoemDTO.class)
            .check(p -> p.getTitle() != null && !p.getTitle().isBlank(), "Title is required")
            .check(p -> p.getAuthor() != null && !p.getAuthor().isBlank(), "Author is required")
            .check(p -> p.getContent() != null && !p.getContent().isBlank(), "Content is required")
            .get();
    }

    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
            .check(value -> value > 0, "Id must be positive")
            .get();

        if (!dao.delete(id)) {
            throw new NotFoundResponse("Poem not found");
        }

        ctx.status(204);
    }
}