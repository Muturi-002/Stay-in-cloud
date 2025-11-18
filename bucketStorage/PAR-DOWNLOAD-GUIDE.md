# File PAR (Pre-authenticated Request) Download Guide

## Overview

***This application supports downloading individual files only!***

This implementation provides a secure, efficient way to handle individual file downloads while reducing server load and improving user experience with time-limited, file-specific access.files through OCI Object Storage Pre-authenticated Requests (PARs). File PARs provide secure, time-limited access to specific objects without requiring authentication credentials.

## File-Specific PAR

- **Endpoint**: `POST /api/createFilePAR`
- **Purpose**: Provides access to a specific file only
- **Duration**: 1 hour
- **Access Level**: Read-only access to the specified file
- **Use Case**: Secure sharing of media

## How to Use File PARs for Downloads

### Method 1: Via Web Interface

1. **Navigate to the Media Page**: Access the main application page
2. **Load File List**: Click "Refresh List" to see available files
3. **Select File**: Choose the file you want to download from the dropdown
4. **Download via PAR**: Click "Download via PAR" for direct OCI download

### Method 2: Via API Calls

#### Create File-Specific PAR
```bash
curl -X POST "http://localhost:8080/api/createFilePAR" \
  -F "fileName=example.pdf"
```

## Security Features

### Time-Limited Access
- **File PARs**: Expire after 1 hour for enhanced security
- Expired PARs require creating a new PAR for continued access. Creation of new PARs automated.

### Access Control
- **File PARs**: Allow downloading only the specific file
- Read-only access - no upload or modification permissions
- Each file requires its own unique PAR

### Automatic Expiration
- PARs automatically expire after 1 hour
- No manual cleanup required
- Secure by default with short expiration times

## Benefits of Using File PARs

1. **No Authentication Required**: Users don't need OCI credentials
2. **Direct OCI Access**: Bypasses application server for faster downloads
3. **Bandwidth Savings**: Reduces load on your application server
4. **Secure**: Time-limited access with automatic expiration
5. **File-Specific**: Each PAR grants access to only one file
6. **Temporary Access**: Perfect for sharing files for short periods


## Troubleshooting
### Common Issues
1. **PAR URL Not Working**
   - Verify the file exists in the bucket
   - Ensure proper URL formatting

2. **Download Fails**
   - Try creating a new file-specific PAR
   - Check browser security settings

3. **Error Creating PAR**
   - Check OCI credentials and permissions
   - Check OCI service limits

### Error Messages

- `"PAR has EXPIRED"`: The PAR has reached its expiration time
- `"No PARs found"`: No existing PARs, will create new one
- `"Failed to create file PAR"`: Check file exists and permissions

