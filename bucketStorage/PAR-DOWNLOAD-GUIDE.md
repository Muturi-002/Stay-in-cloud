# File PAR (Pre-authenticated Request) Download Guide

## Overview

This application supports downloading indivi### Error Messages

- `"Error creating PAR for file"`: Check file exists and permissions
- `"Failed to create file PAR"`: Verify file name and bucket access

This implementation provides a secure, efficient way to handle individual file downloads while reducing server load and improving user experience with time-limited, file-specific access.files through OCI Object Storage Pre-authenticated Requests (PARs). File PARs provide secure, time-limited access to specific objects without requiring authentication credentials.

## File-Specific PAR

- **Endpoint**: `POST /api/createFilePAR`
- **Purpose**: Provides access to a specific file only
- **Duration**: 1 hour
- **Access Level**: Read-only access to the specified file
- **Use Case**: When you need to share a specific file securely for a short period

## How to Use File PARs for Downloads

### Method 1: Via Web Interface

1. **Navigate to the Media Page**: Access the main application page
2. **Load File List**: Click "Refresh List" to see available files
3. **Select File**: Choose the file you want to download from the dropdown
4. **Download via PAR**: Click "Download via PAR" for direct OCI download

### Method 3: Via API Calls

#### Create File-Specific PAR
```bash
curl -X POST "http://localhost:8080/api/createFilePAR" \
  -F "fileName=example.pdf"
```

## Security Features

### Time-Limited Access
- **File PARs**: Expire after 1 hour for enhanced security
- Expired PARs require creating a new PAR for continued access

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

## Best Practices

### For File Sharing
- Use file PARs for sharing sensitive documents securely
- Create new PARs for each sharing session
- Monitor PAR creation and usage in your logs
- Share PAR URLs through secure channels only

### For Application Integration
- Generate PARs on-demand rather than pre-creating them
- Use file PARs for temporary download links in emails or notifications
- Implement proper error handling for expired PARs
- Log PAR creation for audit purposes

### Security Considerations
- Don't share PAR URLs in insecure channels (email, SMS, etc.)
- Monitor access logs for unusual activity
- Consider even shorter expiration times for highly sensitive files
- Ensure the file exists before creating a PAR

## Troubleshooting

### Common Issues

1. **PAR URL Not Working**
   - Check if PAR has expired
   - Verify the file exists in the bucket
   - Ensure proper URL formatting

2. **Download Fails**
   - Try creating a new file-specific PAR
   - Check browser security settings
   - Verify network connectivity to OCI

3. **Error Creating PAR**
   - Check OCI credentials and permissions
   - Verify bucket exists and is accessible
   - Check OCI service limits

### Error Messages

- `"PAR has EXPIRED"`: The PAR has reached its expiration time
- `"No PARs found"`: No existing PARs, will create new one
- `"Failed to create file PAR"`: Check file exists and permissions

