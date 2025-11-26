# Keystore Information

This directory contains the release keystore for signing the AstroStorm APK.

## Keystore Details
- **Keystore File**: release.jks
- **Alias**: astrostorm
- **Store Password**: astrostorm2024
- **Key Password**: astrostorm2024

## Note
This is an open-source application. The keystore is publicly available and should only be used for this project.

The keystore will be automatically generated during the CI/CD build process if it doesn't exist in the repository. This ensures consistent signing across all builds.

## Manual Generation
If you need to generate the keystore locally, run:

```bash
keytool -genkeypair -v \
  -keystore keystore/release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias astrostorm \
  -storepass astrostorm2024 \
  -keypass astrostorm2024 \
  -dname "CN=AstroStorm, OU=Development, O=AstroStorm, L=Unknown, ST=Unknown, C=US"
```
