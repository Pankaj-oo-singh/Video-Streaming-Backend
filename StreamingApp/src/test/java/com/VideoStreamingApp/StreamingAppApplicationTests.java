package com.VideoStreamingApp;

import com.VideoStreamingApp.services.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StreamingAppApplicationTests {

	@Autowired
	VideoService videoService;

	@Test
	void contextLoads() {

//		videoService.processVideo("265ba909-1db7-45d8-bbb0-e59eeca6c010");

	}

}
