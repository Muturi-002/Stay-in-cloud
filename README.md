# Stay-in-cloud

This is a simple project that seeks to emulate storage, retrieval and management of media in the cloud (e.g. Google Drive, OneDrive).
This will involve using a simple website, connected to an Object Storage bucket created in OCI with credentials from the file `./oci/config`.

This is my very first project using the SpringBoot framework.

## Prerequisites
1. Java version 21
2. OCI Java SDK (compatible with Java 21+)
3. OCI CLI installed
4. OCI Credentials in the `.oci/config` file. User account should have the following privileges/policies:
    ```
    Allow group '<group_name>'/'<user_name>' to manage objects in tenancy
    Allow group '<group_name>'/'<user_name>' to manage buckets in tenancy
    ```

## Project Structure
```text
├── bucketStorage/                      # Main application directory
│   ├── .mvn/                           # Maven wrapper files
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── SDK/
│   │       │       ├── BackendWeb.java           # Handles web redirection to home.html
│   │       │       ├── SDK.java                  # OCI SDK integration code
│   │       │   └── Web/bucketStorage/
│   │       │       └── BucketStorageApplication  # Spring Boot application entry point
│   │       └── resources/
│   │           └── static/
│   │               └── Web/
│   │                   ├── home.html             # Web homepage
│   │                   ├── main.html             # Media retrieval/upload page
│   │                   ├── Stay-in-cloud.png     # Site image
│   │                   └── styles.css            # Stylesheet
│   └── test/
│       └── java/                                # Java test code
│   ├── .gitattributes
│   ├── .gitignore
│   ├── pom.xml                                  # Maven project file
│   ├── mvnw
│   ├── mvnw.cmd
├── README.md
├── .gitignore
```

## Project Design

<insert image>

## Steps
### OCI
1. Confirm whether a profile exists with the user ocid intended to be used in the file `.oci/config` . If not, run this command to setup your user's remote access to your OCI tenancy.
    ``` bash
    oci setup config
    ```
2. Create session for your user.
    ```
    oci session authenticate
    ```
### Java
3. Run the command to install the necessary dependencies
    ``` bash
    mvn package
    ```
4. Run this command to start running on your local browser.
    ```bash
    mvn spring-boot:run
    //alternatively, run this -> `./mvnw spring-boot:run`
    ```
5. Go to your browser and enter this url: `localhost:8080/Web/home.html`

