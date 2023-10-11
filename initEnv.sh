kubectx dev-gcp
kubectl exec --tty deployment/bidrag-cucumber-cloud-feature printenv | grep -E 'AZURE_|_URL|SCOPE|REST_AUTH|user_password' > src/main/resources/application-lokal-nais-secrets.properties
