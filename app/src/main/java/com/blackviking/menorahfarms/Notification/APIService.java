package com.blackviking.menorahfarms.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Scarecrow on 4/1/2018.
 */

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA_IFwz6Y:APA91bERpY25NUavxXlNSZDwOLGcQHxb7kWvOb4FVTOatwRIIpxedTOYWlJ5aVwcFgrPsKi0j3KnOVm6xugvZpFl0y5X519zTofvyTiuCZa09bm_JlMA2O-bIuxCSjBO0z8viDuZjXG_"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);

}
