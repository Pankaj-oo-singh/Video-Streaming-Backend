package com.VideoStreamingApp.services;

import com.VideoStreamingApp.entities.Video;
import com.VideoStreamingApp.repositories.VideoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    @Value("${files.video}")
    String DIR;

    @Value("${file.video.hsl}")
    String HSL_DIR;


    private final VideoRepository videoRepository;


    @PostConstruct
    public void init() {

        File file = new File(DIR);


        try {
            Files.createDirectories(Paths.get(HSL_DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!file.exists()) {
            file.mkdir();
            System.out.println("Folder Created:");
        } else {
            System.out.println("Folder already created");
        }

    }

    @Override
    public Video save(Video video, MultipartFile file) {
        // original file name

        try {


            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();


            // file path
            String cleanFileName = StringUtils.cleanPath(filename);


            //folder path : create

            String cleanFolder = StringUtils.cleanPath(DIR);


            // folder path with  filename
            Path path = Paths.get(cleanFolder, cleanFileName);

            System.out.println(contentType);
            System.out.println(path);

            // copy file to the folder
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);


            // video meta data

            video.setContentType(contentType);
            video.setFilePath(path.toString());
            Video savedVideo = videoRepository.save(video);
            //processing video
            processVideo(savedVideo.getVideoId());

            //delete actual video file and database entry  if exception

            // metadata save
            return savedVideo;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in processing video ");
        }


    }


    @Override
    public Video get(String videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("video not found"));
        return video;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

//    @Override
//    public String processVideo(String videoId) {
//
//        Video video = this.get(videoId);
//        String filePath = video.getFilePath();
//
//        //path where to store data:
//        Path videoPath = Paths.get(filePath);
//
//
////        String output360p = HSL_DIR + videoId + "/360p/";
////        String output720p = HSL_DIR + videoId + "/720p/";
////        String output1080p = HSL_DIR + videoId + "/1080p/";
//
//        try {
////            Files.createDirectories(Paths.get(output360p));
////            Files.createDirectories(Paths.get(output720p));
////            Files.createDirectories(Paths.get(output1080p));
//
//            // ffmpeg command
//            Path outputPath = Paths.get(HSL_DIR, videoId);
//
//            Files.createDirectories(outputPath);
//
//
//            String ffmpegCmd = String.format(
//                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
//                    videoPath, outputPath, outputPath
//            );
//
////            StringBuilder ffmpegCmd = new StringBuilder();
////            ffmpegCmd.append("ffmpeg  -i ")
////                    .append(videoPath.toString())
////                    .append(" -c:v libx264 -c:a aac")
////                    .append(" ")
////                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
////                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
////                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
////                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
////                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
////                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
////                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
////                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");
//
//
//            System.out.println(ffmpegCmd);
//            //file this command
//            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
//            processBuilder.inheritIO();
//            Process process = processBuilder.start();
//            int exit = process.waitFor();
//            if (exit != 0) {
//                throw new RuntimeException("video processing failed!!");
//            }
//
//            return videoId;
//
//
//        } catch (IOException ex) {
//            throw new RuntimeException("Video processing fail!!");
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//
//    }


    @Override
    public String processVideo(String videoId) {

        // Fetch the video details using videoId
        Video video = this.get(videoId);
        String filePath = video.getFilePath();  // Get the file path of the video

        Path videoPath = Paths.get(filePath);  // Convert the file path to a Path object

        try {
            // Define output directory for the HLS segments
            Path outputPath = Paths.get(HSL_DIR, videoId);
            Files.createDirectories(outputPath);  // Create the output directory if it doesn't exist

            // Define the segment duration for HLS in seconds
            int segmentDuration = 10;

            // Build the FFmpeg command for segmenting the video into HLS
            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time %d -hls_list_size 0 -hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/master.m3u8\"",
                    videoPath.toString(), segmentDuration, outputPath.toString(), outputPath.toString()
            );

            // Print the command to verify it
            System.out.println("Executing FFmpeg command: " + ffmpegCmd);

            // Execute the FFmpeg command using ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.inheritIO();  // Direct the process output and error streams to the console

            Process process = processBuilder.start();  // Start the FFmpeg process

            // Wait for the process to finish and get the exit code
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // If the process failed (non-zero exit code), throw an exception
                throw new RuntimeException("Video processing failed with exit code: " + exitCode);
            }

            // Return the videoId if processing is successful
            return videoId;

        } catch (IOException ex) {
            // Catch and handle IO exceptions
            throw new RuntimeException("Video processing failed due to I/O error: " + ex.getMessage(), ex);
        } catch (InterruptedException e) {
            // Catch and handle InterruptedExceptions
            throw new RuntimeException("Video processing interrupted: " + e.getMessage(), e);
        }
    }
}