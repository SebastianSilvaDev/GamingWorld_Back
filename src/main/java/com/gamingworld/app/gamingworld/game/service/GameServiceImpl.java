package com.gamingworld.app.gamingworld.game.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.gamingworld.app.gamingworld.externalapi.domain.model.entity.ExternalAPI;
import com.gamingworld.app.gamingworld.externalapi.domain.persistence.ExternalAPIRepository;
import com.gamingworld.app.gamingworld.externalapi.mapping.ExternalAPIMapper;
import com.gamingworld.app.gamingworld.externalapi.resource.TwitchOAuthResponseResource;
import com.gamingworld.app.gamingworld.game.domain.model.entity.Game;
import com.gamingworld.app.gamingworld.game.domain.persistence.GameRepository;
import com.gamingworld.app.gamingworld.game.domain.service.GameService;

import com.gamingworld.app.gamingworld.externalapi.mapping.TwitchOAuthResponseMapper;
import com.gamingworld.app.gamingworld.game.mapping.GameMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {
    private static final String TWITCH_API_CREDENTIALS_URL = "https://id.twitch.tv/oauth2/token?client_id=8en9cck6wbdrkinl4i0oahhxf3ali1&client_secret=jh7gihohaly38ds1e0v98xcnntn7wr&grant_type=client_credentials";
    private static final String IGDB_GAMES_ENDPOINT = "https://api.igdb.com/v4/games";

    //@Autowired
    //private GameRepository gameRepository;

    @Autowired
    private ExternalAPIRepository externalAPIRepository;

    @Autowired
    private TwitchOAuthResponseMapper twitchOAuthResponseMapper;

    @Autowired
    private ExternalAPIMapper externalAPIMapper;

    @Autowired
    private GameMapper gameMapper;

    @Override
    public List<Game> getAll() {
        return null;
    }

    @Override
    public Optional<Game> findById(Long id) {
        if (id == null)
        {
            return Optional.empty();
        }

        ExternalAPI credentials = externalAPIRepository.findByExternalAPIName("TWITCH_AUTH").get(0);

        if (credentials == null)
        {
            getIGDBCredentials();
        }
        else
        {
            Date today = new Date();

            if (today.getTime() >= credentials.getExpirationDate().getTime())
            {
                getIGDBCredentials();
                credentials = externalAPIRepository.findByExternalAPIName("TWITCH_AUTH").get(0);
            }
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(IGDB_GAMES_ENDPOINT);

            StringEntity requestEntity = new StringEntity(
                    "fields name; where id=" + id + ";",
                    ContentType.APPLICATION_JSON);
            
            httpPost.setEntity(requestEntity);
            httpPost.setHeader("Authorization", "Bearer " + credentials.getToken());
            httpPost.setHeader("Client-ID", "8en9cck6wbdrkinl4i0oahhxf3ali1");

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

                System.out.println("Response from IGDB API Games Endpoint was: " + response.getCode());

                String result = getResponseBodyFromRequest(response);
                result = result.substring(1, result.length() - 1); // We get response as an Array, so we need to remove those brackets.

                Game gameRetrieved = gameMapper.toModel(result);

                return Optional.ofNullable(gameRetrieved);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Game save(Game entity) {
        return new Game();
    }

    @Override
    public void getIGDBCredentials() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(TWITCH_API_CREDENTIALS_URL);

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                System.out.println("Response from Twitch OAuth API was: " + response.getCode());

                String result = getResponseBodyFromRequest(response);

                TwitchOAuthResponseResource twitchResponse = twitchOAuthResponseMapper.toResource(result);
                externalAPIRepository.save(externalAPIMapper.toModel(twitchResponse));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getResponseBodyFromRequest(CloseableHttpResponse response)
    {
        StringBuilder result = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
