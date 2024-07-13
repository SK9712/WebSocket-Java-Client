package com.web.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApiExecutor {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Gson gson;

    public JsonObject processApiRequest(JsonObject request){
        return null;
    }
}
