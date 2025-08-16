package SDK;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class SDK {
    //Setting the variables above as static allows them to be used in the SDK classes.
    static String bucketName = "BucketStore-1";
    static ConfigFileAuthenticationDetailsProvider provider;
    static ObjectStorageClient client;
    static ListObjectsResponse osResponse;

    @PutMapping("/api/setBucket")
    private static void setBucket() throws IOException{
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        try {
            client.getBucket(GetBucketRequest.builder()
                    .namespaceName(namespace)  //get objectstorage namespace
                    .bucketName(bucketName)
                    .build());
            System.out.println("Bucket '" + bucketName + "' already exists.");
        } catch (Exception e) {
            // Bucket does not exist, create it
            CreateBucketRequest bucketCreate= CreateBucketRequest.builder()
                    .namespaceName(namespace)
                    .createBucketDetails(CreateBucketDetails.builder()
                            .name(bucketName)
                            .compartmentId(provider.getTenantId())
                            .build())
                    .build();
            CreateBucketResponse bucketResponse = client.createBucket(bucketCreate);
            System.out.println("Bucket '" + bucketResponse.getBucket().getName() + "' is created and ready for use.");
        }
        listBuckets();
    }

    private static void listBuckets(){
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder()
                .namespaceName(namespace)
                .compartmentId(provider.getTenantId())
                .build();
        ListBucketsResponse response = client.listBuckets(listBucketsRequest);
        if (response.getItems().isEmpty()) {
            System.out.println("No buckets found in the compartment.");
            try {
                setBucket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Listing buckets in root compartment --->:");
            for (var bucket : response.getItems()) {
                System.out.println("Bucket Name: " + bucket.getName());
                System.out.println("Created On: " + bucket.getTimeCreated());
                System.out.println("-----------------------------");
            }
        }
    }

    @PostMapping("/api/upload")
    public String uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
        setBucket();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName(client.getNamespace(GetNamespaceRequest.builder().build()).getValue())
                .bucketName(bucketName)
                .objectName(file.getOriginalFilename()) // Name of the object to be created
                .putObjectBody(file.getInputStream()) // Provide content for the object
                .build();
        PutObjectResponse response = client.putObject(putObjectRequest);
        listObjects();
        return "Upload succeeded, ETag: " + response.getETag() +"\n Object name: " + file.getOriginalFilename();
    }
    @GetMapping("/api/listmedia")
    private List<String> listObjects() throws IOException {
        setBucket();
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .build();
        osResponse = client.listObjects(listObjectsRequest);
        return osResponse.getListObjects().getObjects()
                .stream()
                .map(ObjectSummary::getName)
                .toList();
    }

    private static void deleteObjects(){
        // Delete objects from the bucket
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        String objectName = "slim.txt"; // Name of the object to be deleted
        if (osResponse.getListObjects().getObjects().stream()
                .anyMatch(object -> object.getName().equals(objectName))){
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .build();
            client.deleteObject(deleteObjectRequest);
            System.out.println("Object '" + objectName + "' has been deleted from bucket '" + bucketName + "'.");
        }else {
            System.out.println("Object '" + objectName + "' does not exist in bucket '" + bucketName + "'.");
        }
    }

    public static void main(String[] args) throws IOException {
        // Load auth provider from default OCI config file (~/.oci/config). Eases connection to OCI services by using already set credentials.
       provider = new ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT");

        // Create Object Client
        client =  ObjectStorageClient.builder().build(provider);
        client.setRegion(provider.getRegion());

        //Create Bucket "BucketStore-1" if not exists
        setBucket();
//        putObjects();
//        deleteObjects();
    }
}
