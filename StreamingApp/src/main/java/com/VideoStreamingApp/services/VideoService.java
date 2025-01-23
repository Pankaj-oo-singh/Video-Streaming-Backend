package com.VideoStreamingApp.services;

import com.VideoStreamingApp.entities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    //save  video
    Video save(Video video, MultipartFile file);


    // get video by  id
    Video get(String videoId);


    // get video by title

    Video getByTitle(String title);

    List<Video> getAll();


    //video processing
    String processVideo(String videoId);


}
