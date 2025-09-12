package Web.bucketStorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"Web.bucketStorage", "SDK"})
public class BucketStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BucketStorageApplication.class, args);
	}

}
