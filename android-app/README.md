# Android App

## Backup Policy

The application disables Android's automatic backup feature
(`android:allowBackup="false"`) to avoid uploading sensitive
financial information to external storage or cloud services. If backups
are required in the future, ensure that any data written to disk is
encrypted or explicitly excluded via a `fullBackupContent` rules file.

