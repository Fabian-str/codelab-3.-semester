package app;

import app.config.HibernateConfig;
import app.controllers.PoemController;
import app.daos.PoemDAO;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        PoemDAO dao = new PoemDAO(emf);
        PoemController controller = new PoemController(dao);

        Javalin app = Javalin.create();

        app.get("/poem", controller::readAll);
        app.post("/poem", controller::create);
        app.get("/poem/{id}", controller::read);
        app.put("/poem/{id}", controller::update);
        app.delete("/poem/{id}", controller::delete);

        app.events(events -> events.serverStopped(emf::close));

        app.start(7070);
    }
}