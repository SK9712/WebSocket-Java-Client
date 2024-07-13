package com.test.poc;

import com.google.gson.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class P2PWeb {

    @Autowired
    private ApiExecutor apiExecutor;


    @RequestMapping(method = RequestMethod.POST, value = "poc/getClients",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonObject> p2pWeb(@RequestBody JsonObject request) {
        JsonObject result = apiExecutor.processApiRequest(request);

        ResponseEntity<JsonObject> response = new ResponseEntity<>(result, HttpStatus.OK);

        return response;
    }
}
