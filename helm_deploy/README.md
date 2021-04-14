# Deploying with Helm

These tests are expected to run from the `court-probation-preprod` namespace. A manual step is required to create or update the username and password secret in the namespace using the [create_user_credentials_secret.sh](create_user_credentials_secret.sh) shell script.

## Create user credentials secret

To create the secret run the following command, this will create or overwrite any existing secret called `perf-test-data-user-credentials`. Note: this assumes you have a kubectl and a local context set up called `court-probation-preprod`

```
create_secret.sh <username> <password>
```
