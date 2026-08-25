package app.dao;

import java.util.List;

public interface IDAO<T> {

    T create(T entity);

    List<T> getAll();

    T getById(int id);

    T update(T entity);

    boolean delete(int id);
}