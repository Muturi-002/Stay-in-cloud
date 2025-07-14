# Stay-in-cloud

This is a simple project that seeks to emulate storage, retrieval and management of media in the cloud (e.g. Google Drive, OneDrive).
This will involve using a simple (not decorated as much) web application, connected to an Object Storage bucket created in OCI after signing up.

## Project Structure
```tree
├── OCI-Java/                   # the directory for OCI-Java SDK code
│   └── 
├── Web/
│   └── styles.css
│   └── auth.html               # the login or sign-up page
│   └── home.html               # the web homepage
│   └── main.html               # the main page after login
│   └── web.go                  # backend of the website
│   └── Stay-in-cloud.png       # Site image
├── README.md
├── .gitignore

```