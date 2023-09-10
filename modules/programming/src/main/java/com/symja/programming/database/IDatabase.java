package com.symja.programming.database;

import org.json.JSONException;

import java.util.List;

public interface IDatabase<I> {
    List<I> getAll();

    void insert(I item) throws JSONException;

    void update(I item) throws JSONException;

    void delete(I item) throws JSONException;

    void clear() throws JSONException;
}
