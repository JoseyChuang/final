package com.example.final_project.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import com.example.final_project.BuildConfig;

public interface OpenAIApiService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer " + BuildConfig.OPENAI_API_KEY
    })
    @POST("v1/chat/completions")
    Call<com.example.final_project.api.OpenAIResponse> getChatResponse(
            @Body com.example.final_project.api.OpenAIRequest request
    );
}