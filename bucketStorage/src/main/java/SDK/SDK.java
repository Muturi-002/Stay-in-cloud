package SDK;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.*;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;

@RestController
@Component
public class SDK implements CommandLineRunner {
    //Setting the variables above as static allows them to be used in the SDK classes.
    private static final String bucketName = "BucketStore-1";
    static ConfigFileAuthenticationDetailsProvider provider;
    static ObjectStorageClient client;
    static ListObjectsResponse osResponse;

    static String bucketPAR= bucketName+"PAR";
    //Checking most recent PAR expiry
    static Calendar calendar = Calendar.getInstance();
    static Date expiryDate ;

    // Initialize
    @Override
    public void run(String... args) throws Exception {
        // Load auth provider from default OCI config file (~/.oci/config). Eases connection to OCI services by using already set credentials.
        provider = new ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT");

        // Create Object Client
        client =  ObjectStorageClient.builder().build(provider);
        client.setRegion(provider.getRegion());

        //Create Bucket "BucketStore-1" if not exists
        setBucket();
    }
    @GetMapping("/api/bucketInit")
    public String setBucket() {
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        try {
            client.getBucket(GetBucketRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .build());
            return "Bucket already exists: " + bucketName;
        } catch (BmcException e) {
            if (e.getStatusCode() == 404) {
                // Bucket doesn't exist, create it
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .namespaceName(namespace)
                        .createBucketDetails(CreateBucketDetails.builder()
                                .name(bucketName)
                                .compartmentId(provider.getTenantId())
                                .build())
                        .build();
                client.createBucket(createBucketRequest);
                return "Bucket created: " + bucketName;
            } else {
                return "Error checking/creating bucket: " + e.getMessage();
            }
        }
    }

    private void listBuckets(){
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder()
                .namespaceName(namespace)
                .compartmentId(provider.getTenantId())
                .build();
        ListBucketsResponse response = client.listBuckets(listBucketsRequest);
        if (response.getItems().isEmpty()) {
            System.out.println("No buckets found in the compartment.");
            setBucket();
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
    public String uploadFiles(@RequestParam("file") MultipartFile file) {
        String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
        
        // Initialize bucket first
        setBucket();
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(file.getOriginalFilename()) // Use the file's original name as the object name
                .contentLength(file.getSize()) // Set content length
                .putObjectBody(file.getInputStream()) // Provide content for the object
                .build();

            client.putObject(putObjectRequest);
            return "File uploaded successfully: " + file.getOriginalFilename();
        } catch (IOException e) {
            return "Error uploading file: " + e.getMessage();
        }
    }

    @GetMapping("/api/listmedia")
    public List<String> listObjects(){
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

    private static void createPAR(){
        calendar.add(Calendar.DATE,7);
        expiryDate= calendar.getTime();
        CreatePreauthenticatedRequestDetails createPAR= CreatePreauthenticatedRequestDetails.builder()
                .name(bucketPAR)
                .bucketListingAction(PreauthenticatedRequest.BucketListingAction.ListObjects)
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.AnyObjectReadWrite)
                .timeExpires(expiryDate)
                .build();

        CreatePreauthenticatedRequestRequest createPARrequest= CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(client.getNamespace(GetNamespaceRequest.builder().build()).getValue())
                .bucketName(bucketName)
                .createPreauthenticatedRequestDetails(createPAR)
                .build();

        CreatePreauthenticatedRequestResponse responsePAR= client.createPreauthenticatedRequest(createPARrequest);
    }

    @PostMapping("/api/bucketPAR")
    public String getPAR(){
        ListPreauthenticatedRequestsRequest listPAR = ListPreauthenticatedRequestsRequest.builder()
                .namespaceName(client.getNamespace(GetNamespaceRequest.builder().build()).getValue())
                .bucketName(bucketName)
                .build();

        ListPreauthenticatedRequestsResponse listPARresponse= client.listPreauthenticatedRequests(listPAR);
        Optional<PreauthenticatedRequestSummary> mostRecentPAR = listPARresponse.getItems().stream()
                .max(Comparator.comparing(PreauthenticatedRequestSummary::getTimeCreated));
        PreauthenticatedRequestSummary par = mostRecentPAR.get();
        if (listPARresponse.getItems().isEmpty()) {
            System.out.println("No PARs found for bucket `"+bucketName+"`. Creating new PAR....\n");
            createPAR();
        }else {
            Date expiryTime = par.getTimeExpires();

            System.out.println("Most recent PAR found:");
            System.out.println("Time created: " + par.getTimeCreated());
            System.out.println("Time expired: " + expiryTime);
            System.out.println("Current time: " + new Date());
            System.out.println("-------------");

            // Check if the PAR has expired
            if (expiryTime.before(new Date())) {
                System.out.println("This PAR has EXPIRED. You may need to create a new one.");
                createPAR();
            }
        }
        return mostRecentPAR.get().toString();
    }

    @GetMapping("/api/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileName") String fileName) {
        try {
            String namespace = client.getNamespace(GetNamespaceRequest.builder().build()).getValue();
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(fileName)
                .build();
            
            GetObjectResponse response = client.getObject(getObjectRequest);
            
            InputStreamResource resource = new InputStreamResource(response.getInputStream());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
